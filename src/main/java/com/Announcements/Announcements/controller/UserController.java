package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.MyException.CaptchaException;
import com.Announcements.Announcements.dto.*;
import com.Announcements.Announcements.mapper.NewsMapper;
import com.Announcements.Announcements.mapper.UserMapper;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final NewsService newsService;
    private final EmailService emailService;
    private final CaptchaService captchaService;
    private final UserMapper userMapper;
    private final NewsMapper newsMapper;

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
    @PostMapping("/api/v1/users/create-news")
    public NewsDTO addNews(@RequestBody CreateNewsDTO newsDto, @RequestHeader("g-recaptcha-response") String captchaResponse) throws Exception {
        if (!captchaService.validateCaptcha(captchaResponse)) {
            log.error("Captcha не пройдена!");
            throw new CaptchaException("Captcha validation failed.");
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO user = userService.findByUsername(username);
        log.info("{} создает новую новость: {}", username,newsDto.name());

        News news = new News(newsDto);
        news.setUser(userMapper.fromUserDto(user));
        NewsDTO newsDTO = newsService.addNews(news);
        log.info("Новость успешно создана: {} пользователем {}", newsDto.name(), username);
        return newsDTO;
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
    @PostMapping("/api/v1/users/news/{id}/send-email")
    public String sendEmailToAuthor(@PathVariable Integer id, @RequestBody String message) throws Exception {
        log.info("Отправка письма для новости ID: {}", id);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        NewsDTO newsDto  = newsService.getSimpleNews(id, username);
        if (newsDto == null) {
            log.warn("Новость с id={}, которую хочет прокомментировать пользователь, не найдена(", id);
            return "Новость не найдена";
        }

        String author = newsDto.username();
        String gmail = newsDto.gmail();
        if (author == null || gmail == null) {
            log.warn("Пользователь пытается отправить комментарий " +
                    "на не корректных пользователя = {} или почту = {}", author, gmail);
            return "Автор не верный или почта не верна!";
        }

        if (newsDto.username().equals(username)) {
            log.warn("Пользователь {} пытается отправить сообщение на свою новость", username);
            return "Вы не можете комментировать свои новости!";
        }

        emailService.sendSimpleEmail(
                gmail,
                "Комментарий от пользователя!",
                message,
                username
        );
        log.info("Письмо отправлено автору новости id={} от пользователя {}", id, username);
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
    @GetMapping("/api/v1/users/my-news")
    public List<NewsDTO> getNewsUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Получение списка новостей для пользователя: {}", username);
        UserDTO user = userService.findByUsername(username);
        if (user == null) {
            return List.of();
        }
        List<NewsDTO> listDto = newsService.getNewsByUserId(userMapper.fromUserDto(user).getId());
        log.info("Пользователь {} получил {} новостей.", username, listDto.size());
        return listDto;
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
    @PutMapping("/api/v1/users/my-news/status/{id}")
    public NewsDTO updateStatusNews(@PathVariable Integer id, @RequestBody Status updatedNewsStatus) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO user = userService.findByUsername(username);
        log.info("Пользователь {} изменяет статус новости id={} на {}", username, id, updatedNewsStatus);

        NewsDTO existingNews = newsService.getSimpleNews(id, username);

        if (Objects.isNull(existingNews) || !Objects.equals(existingNews.username(), userMapper.fromUserDto(user).getUsername())) {
            log.warn("Попытка редактирования чужой новости. Пользователь {} пытался редактировать новость автора {}.",
                    user.username(),
                    existingNews.username());
            throw new IllegalArgumentException("Вы не можете редактировать эту новость.");
        }

        NewsDTO newsDTO = newsService.updateNews(id, updatedNewsStatus);
        log.info("Новость id={} успешно обновлена пользователем {}", id, username);
        return newsDTO;
    }
}

