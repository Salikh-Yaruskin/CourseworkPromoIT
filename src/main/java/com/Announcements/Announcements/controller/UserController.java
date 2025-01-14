package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.MyException.CaptchaException;
import com.Announcements.Announcements.dto.*;
import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Status;
import com.Announcements.Announcements.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Tag(name = "Контроллер пользователя", description = "Функции доступные авторизованному пользователю")
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final NewsService newsService;
    private final EmailService emailService;
    private final CaptchaService captchaService;
    private final AuthServiceClient authServiceClient;

    @Operation(
            summary = "Создание новости с проверкой reCaptcha",
            description = "Позволяет авторизованным пользователям создать новость. " +
                    "Для создания новости необходимо пройти проверку reCaptcha. Используйте тестовый ключ: test-captcha-token.",
            parameters = {
                    @Parameter(name = "g-recaptcha-response", description = "Токен reCaptcha", required = true, example = "test-captcha-token")
            },
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
        log.info("Captcha прошла!");
        return newsService.addNews(newsDto);
    }

    @Operation(
            summary = "Отправка сообщения автору новости",
            description = "Позволяет отправить email автору выбранной новости. Отправка доступна только для новостей, которые не принадлежат текущему пользователю.",
            responses = {
                    @ApiResponse(description = "Письмо успешно отправлено", responseCode = "200"),
                    @ApiResponse(description = "Новость не найдена или почта автора не указана", responseCode = "404"),
                    @ApiResponse(description = "Попытка отправить сообщение на свою новость", responseCode = "400")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Ваш комментарий автору новости",
                required = true,
                content = @Content(
                        examples = @ExampleObject(
                                name = "Пример сообщения",
                                value = "Хай! Отличная новость, закуплюсь криптой:)"
                        )
                )
            )
    )
    @PostMapping("/api/v1/users/news/{id}/send-email")
    public String sendEmailToAuthor(@PathVariable Integer id, @RequestBody String message) throws Exception {
        log.info("Поптыка отправки письма для новости ID: {}", id);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        NewsDTO newsDto  = newsService.getSimpleNews(id);
        if (newsDto == null) {
            log.warn("Новость с id={}, которую хочет прокомментировать пользователь, не найдена(", id);
            return "Новость не найдена";
        }

        UserDTO userDto = authServiceClient.getUserById(newsDto.user());
        String author = userDto.username();
        String gmail = userDto.gmail();
        if (author == null || gmail == null) {
            log.warn("Пользователь пытается отправить комментарий " +
                    "на не корректных пользователя = {} или почту = {}", author, gmail);
            return "Автор не верный или почта не верна!";
        }

        if (userDto.username().equals(username)) {
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
            summary = "Просмотр новостей созданных данным пользователем",
            description = "Позволяет авторизованному пользователю просмотреть свои объявления с отображением количества просмотров.",
            responses = {
                    @ApiResponse(description = "Список новостей успешно получен", responseCode = "200"),
                    @ApiResponse(description = "Пользователь не найден", responseCode = "404")
            }
    )
    @GetMapping("/api/v1/users/my-news")
    public List<NewsDTO> getNewsUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Попытка получение списка новостей для пользователя: {}", username);
        Optional<UserDTO> userOpt = authServiceClient.getUserByName(username);
        if (userOpt.isEmpty()) {
            return List.of();
        }
        UserDTO user = userOpt.get();
        List<NewsDTO> listDto = newsService.getNewsByUserId(user.id());
        log.info("Пользователь {} получил {} новостей.", username, listDto.size());
        return listDto;
    }

    @Operation(
            summary = "Изменение статуса новости",
            description = "Позволяет автору новости изменить её статус. Доступно только для новостей, которые принадлежат текущему пользователю.",
            parameters = {
                    @Parameter(name = "id", description = "Идентификатор новости", example = "1")
            },
            responses = {
                    @ApiResponse(description = "Статус новости успешно обновлен", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NewsDTO.class))),
                    @ApiResponse(description = "Вы не авторизованы!", responseCode = "401",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class))),
                    @ApiResponse(description = "Пользователь не может редактировать чужие новости", responseCode = "403",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Error.class)))
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Новый статус новости", required = true)
    )
    @PutMapping("/api/v1/users/my-news/status/{id}")
    public NewsDTO updateStatusNews(@PathVariable Integer id, @RequestBody Status updatedNewsStatus) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserDTO> userOpt = authServiceClient.getUserByName(username);
        UserDTO user = userOpt.get();
        log.info("Попытка пользователя {} изменяет статус новости id={} на {}", username, id, updatedNewsStatus);

        NewsDTO existingNews = newsService.getSimpleNews(id);
        UserDTO userDTO = authServiceClient.getUserById(existingNews.user());

        if (Objects.isNull(existingNews) || !Objects.equals(userDTO.username(), user.username())) {
            log.warn("Попытка редактирования чужой новости. Пользователь {} пытался редактировать новость автора {}.",
                    user.username(),
                    userDTO.username());
            throw new IllegalArgumentException("Вы не можете редактировать эту новость.");
        }

        NewsDTO newsDTO = newsService.updateNews(id, updatedNewsStatus);
        log.info("Новость id={} успешно обновлена пользователем {}", id, username);
        return newsDTO;
    }
}

