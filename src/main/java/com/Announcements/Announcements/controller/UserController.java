package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.MyException.CaptchaException;
import com.Announcements.Announcements.dto.CreateNewsDTO;
import com.Announcements.Announcements.dto.LoginDTO;
import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.dto.UserDTO;
import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Status;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.service.CaptchaService;
import com.Announcements.Announcements.service.EmailService;
import com.Announcements.Announcements.service.NewsService;
import com.Announcements.Announcements.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CaptchaService captchaService;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Позволяет зарегистрировать нового пользователя в системе",
            responses = {
                    @ApiResponse(description = "Успешная регистрация", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = UserDTO.class))),
                    @ApiResponse(description = "Ошибка валидации или регистрация не удалась", responseCode = "400")
            }
    )
    @PostMapping("/register")
    public UserDTO register(@RequestBody UserDTO userDTO){
        return userService.register(userDTO);
    }

    @Operation(
            summary = "Авторизация пользователя",
            description = "Позволяет авторизовать пользователя по логину и паролю",
            responses = {
                    @ApiResponse(description = "Успешная авторизация", responseCode = "200"),
                    @ApiResponse(description = "Неверные данные авторизации", responseCode = "401")
            }
    )
    @PostMapping("/login")
    public String login(@RequestBody LoginDTO loginDTO){
        return userService.verify(loginDTO);
    }

    @Operation(
            summary = "Создание новости с проверкой reCaptcha",
            description = "Позволяет авторизованным пользователям создать новость. Для создания новости необходимо пройти проверку reCaptcha. Используйте тестовый ключ: test-captcha-token.",
            responses = {
                    @ApiResponse(description = "Новость успешно создана", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NewsDTO.class))),
                    @ApiResponse(description = "Ошибка валидации Captcha", responseCode = "429"),
                    @ApiResponse(description = "Пользователь не авторизован", responseCode = "401")
            }
    )
    @PostMapping("/user/create-news")
    public NewsDTO addNews(@RequestBody CreateNewsDTO newsDto, @RequestHeader("g-recaptcha-response") String captchaResponse) throws Exception {
        if (!captchaService.validateCaptcha(captchaResponse)) {
            throw new CaptchaException("Captcha validation failed.");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userService.findByUsername(username);

        News news = new News(newsDto);
        news.setUser(user);
        News createdNews = newsService.addNews(news);

        return new NewsDTO(
                createdNews.getId(),
                createdNews.getName(),
                createdNews.getDescription(),
                createdNews.getUser().getUsername(),
                createdNews.getUser().getGmail(),
                createdNews.getViewCount(),
                createdNews.getStatus()
        );
    }

    @Operation(
            summary = "Отправка сообщения автору новости",
            description = "Позволяет отправить email автору выбранной новости. Отправка доступна только для новостей, которые не принадлежат текущему пользователю.",
            responses = {
                    @ApiResponse(description = "Письмо успешно отправлено", responseCode = "200"),
                    @ApiResponse(description = "Новость не найдена или почта автора не указана", responseCode = "404"),
                    @ApiResponse(description = "Попытка отправить сообщение на свою новость", responseCode = "400")
            }
    )
    @PostMapping("/user/news/{id}/send-email")
    public String sendEmailToAuthor(@PathVariable Integer id, @RequestBody String message) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        NewsDTO newsDto  = newsService.getNews(id, username);
        if (newsDto == null) {
            return "Новость не найдена";
        }

        String author = newsDto.username();
        String gmail = newsDto.gmail();
        if (author == null || gmail == null) {
            return "Автор не верный или почта не верна!";
        }

        if (newsDto.username().equals(username)) {
            return "Вы не можете комментировать свои новости!";
        }

        emailService.sendSimpleEmail(
                gmail,
                "Комментарий от пользователя!",
                message,
                username
        );

        return "Письмо отправлено: " + username;
    }

    @Operation(
            summary = "Просмотр собственных новостей",
            description = "Позволяет авторизованному пользователю просмотреть свои объявления с отображением количества просмотров.",
            responses = {
                    @ApiResponse(description = "Список новостей успешно получен", responseCode = "200"),
                    @ApiResponse(description = "Пользователь не найден", responseCode = "404")
            }
    )
    @GetMapping("/user/my-news")
    public List<NewsDTO> getNewsUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = userService.findByUsername(username);
        if (user == null) {
            return List.of();
        }
        return newsService.getNewsByUserId(user.getId());
    }

    @Operation(
            summary = "Изменение статуса новости",
            description = "Позволяет автору новости изменить её статус. Доступно только для новостей, которые принадлежат текущему пользователю.",
            responses = {
                    @ApiResponse(description = "Статус новости успешно обновлен", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NewsDTO.class))),
                    @ApiResponse(description = "Пользователь не может редактировать чужие новости", responseCode = "403")
            }
    )
    @PutMapping("/user/my-news/status/{id}")
    public NewsDTO updateStatusNews(@PathVariable Integer id, @RequestBody Status updatedNewsStatus) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userService.findByUsername(username);

        NewsDTO existingNews = newsService.getNews(id, username);

        if (existingNews == null || !Objects.equals(existingNews.username(), user.getUsername())) {
            throw new IllegalArgumentException("Вы не можете редактировать эту новость.");
        }

        News updatedNews = newsService.updateNews(id, updatedNewsStatus);
        return new NewsDTO(
                updatedNews.getId(),
                updatedNews.getName(),
                updatedNews.getDescription(),
                updatedNews.getUser().getUsername(),
                updatedNews.getUser().getGmail(),
                updatedNews.getViewCount(),
                updatedNews.getStatus()
        );
    }
}

