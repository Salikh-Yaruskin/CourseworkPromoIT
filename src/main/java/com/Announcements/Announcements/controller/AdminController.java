package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.dto.UpdateNewsDTO;
import com.Announcements.Announcements.dto.UserDTO;
import com.Announcements.Announcements.mapper.NewsMapper;
import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.service.AuthServiceClient;
import com.Announcements.Announcements.service.NewsService;
import com.Announcements.Announcements.service.UserService;
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
import java.util.NoSuchElementException;

@Tag(name = "Контроллер администратора", description = "Действия, которые может выполнять администратор")
@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final NewsService newsService;
    private final AuthServiceClient authServiceClient;

    @Operation(
            summary = "Редактирование объявления",
            description = "Позволяет администратору редактировать существующее объявление по его ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID новости", required = true),
            },
            responses = {
                    @ApiResponse(description = "Новость успешно обновлена", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NewsDTO.class))),
                    @ApiResponse(description = "Новость не найдена", responseCode = "404")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные новости, которые нужно обновить",
                    required = true
            )
    )
    @PutMapping("/api/v1/admins/news/{id}")
    public NewsDTO updateAdminNews(@PathVariable Integer id, @RequestBody UpdateNewsDTO updateNewsDto) throws Exception {
        log.info("Администратор запрашивает редактирование новости ID: {}", id);
        return newsService.updateNews(updateNewsDto, id);
    }

    @Operation(
            summary = "Удаление объявления",
            description = "Позволяет администратору удалить существующее объявление по его ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID новости", example = "3", required = true),
            },
            responses = {
                    @ApiResponse(description = "Новость успешно удалена", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NewsDTO.class))),
                    @ApiResponse(description = "Новость не найдена", responseCode = "404")
            }
    )
    @DeleteMapping("/api/v1/admins/news/{id}")
    public NewsDTO deleteNews(@PathVariable Integer id) {
        log.info("Администратор запрашивает удаление новости ID: {}", id);
        return newsService.deleteNews(id);
    }

    @Operation(
            summary = "Блокирование пользователя",
            description = "Позволяет администратору заблокировать пользователя по его ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID пользователя", required = true),
            },
            responses = {
                    @ApiResponse(description = "Пользователь успешно заблокирован", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = UserDTO.class))),
                    @ApiResponse(description = "Нельзя заблокировать самого себя", responseCode = "400"),
                    @ApiResponse(description = "Пользователь не найден", responseCode = "404")
            }
    )
    @PutMapping("/api/v1/admins/users/{id}/blocked")
    public String blockedUser(@PathVariable Integer id) {
        log.info("Администратор запрашивает блокировку пользователя ID: {}", id);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO userDTO = authServiceClient.blockUser(id, username);
        log.info("Пользователь с ID: {} успешно заблокирован.", id);
        return userDTO.username();
    }

    @Operation(
            summary = "Разблокирование пользователя",
            description = "Позволяет администратору разблокировать пользователя по его ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID пользователя", required = true),
            },
            responses = {
                    @ApiResponse(description = "Пользователь успешно разблокирован", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = UserDTO.class))),
                    @ApiResponse(description = "Нельзя разблокировать самого себя", responseCode = "400"),
                    @ApiResponse(description = "Пользователь не найден", responseCode = "404")
            }
    )
    @PutMapping("/api/v1/admins/users/{id}/unblocked")
    public String unblockedUser(@PathVariable Integer id) {
        log.info("Администратор запрашивает разблокировку пользователя ID: {}", id);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO userDTO = authServiceClient.unblock(id, username);
        log.info("Пользователь с ID: {} успешно разблокирован.", id);
        return userDTO.username();
    }

    @Operation(
            summary = "Настройка лимита объявлений для пользователя",
            description = "Позволяет администратору установить лимит на количество объявлений, которые пользователь может создавать в день.",
            parameters = {
                    @Parameter(name = "id", description = "ID пользователя", example = "2", required = true),
            },
            responses = {
                    @ApiResponse(description = "Лимит успешно установлен", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = UserDTO.class))),
                    @ApiResponse(description = "Пользователь не найден", responseCode = "404")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новый лимит на количество объявлений",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = "15"
                            )
                    )
            )
    )
    @PutMapping("/api/v1/admins/users/{id}/limits")
    public UserDTO userLimit(@PathVariable Integer id, @RequestBody Integer updateLimit) {
        log.info("Администратор устанавливает лимит на количество объявлений для пользователя ID: {}. Новый лимит: {}", id, updateLimit);
        UserDTO userDTO = authServiceClient.updateLimits(id, updateLimit);
        log.info("Лимит для пользователя ID: {} успешно обновлен на {}.", id, updateLimit);
        return userDTO;
    }

    @Operation(
            summary = "Просмотр архива объявлений",
            description = "Позволяет администратору просмотреть архив всех объявлений.",
            responses = {
                    @ApiResponse(description = "Архив объявлений успешно получен", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NewsDTO.class)))
            }
    )
    @GetMapping("/api/v1/admins/archives")
    public List<NewsDTO> getArchive() {
        log.info("Администратор запрашивает просмотр архива объявлений.");
        List<NewsDTO> archive = newsService.getArchive();
        log.info("Администратор получил {} архивных объявлений.", archive.size());
        return archive;
    }

    @Operation(
            summary = "Выдача роли пользователю",
            description = "Позволяет администратору выдать пользователю роль по его ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID пользователя", example = "2", required = true),
            },
            responses = {
                    @ApiResponse(description = "Роль успешно обновлена", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = UserDTO.class))),
                    @ApiResponse(description = "Пользователь не найден", responseCode = "404")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новая роль",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                name = "Укажите новую роль",
                                    value = "ADMIN"
                            )
                    )
            )
    )
    @PutMapping("/api/v1/admins/users/{id}/role")
    public UserDTO updateUserRole(@PathVariable Integer id, @RequestBody String role) {
        log.info("Администратор назначает новую роль для пользователя ID: {}. Новая роль: {}", id, role);
        UserDTO userDTO = authServiceClient.updateRole(id, role);
        log.info("Роль для пользователя ID: {} успешно обновлена на {}.", id, role);
        return userDTO;
    }

    @Operation(
            summary = "Получение списка всех пользователей",
            description = "Позволяет администратору получить список всех зарегистрированных пользователей.",
            responses = {
                    @ApiResponse(description = "Список пользователей успешно получен", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = UserDTO.class)))
            }
    )
    @GetMapping("/api/v1/admins/users")
    public List<UserDTO> getAllUser() {
        log.info("Администратор запрашивает список всех пользователей.");
        List<UserDTO> users = authServiceClient.getAllUsers();
        log.info("Администратор получил список из {} пользователей.", users.size());
        return users;
    }
}

