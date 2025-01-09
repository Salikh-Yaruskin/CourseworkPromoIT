package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.MyException.UserSelfException;
import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.dto.RoleDTO;
import com.Announcements.Announcements.dto.UpdateNewsDTO;
import com.Announcements.Announcements.dto.UserDTO;
import com.Announcements.Announcements.mapper.NewsMapper;
import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.service.AuthServiceClient;
import com.Announcements.Announcements.service.NewsService;
import com.Announcements.Announcements.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final NewsService newsService;
    private final NewsMapper newsMapper;
    private final AuthServiceClient authServiceClient;

    @Operation(
            summary = "Редактирование объявления",
            description = "Позволяет администратору редактировать существующее объявление по его ID.",
            responses = {
                    @ApiResponse(description = "Новость успешно обновлена", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NewsDTO.class))),
                    @ApiResponse(description = "Новость не найдена", responseCode = "404")
            }
    )
    @PutMapping("/api/v1/admins/news/{id}")
    public NewsDTO updateAdminNews(@PathVariable Integer id, @RequestBody UpdateNewsDTO updateNewsDto) {
        log.info("Администратор запрашивает редактирование новости ID: {}", id);
        News news = newsService.getNews(id);
        if (news == null) {
            log.warn("Новость с ID {} не найдена.", id);
            throw new NoSuchElementException("Нет такой новости!");
        }
        log.info("Обновление новости ID: {} с новыми данными: {}", id, updateNewsDto);
        news.setName(updateNewsDto.name());
        news.setDescription(updateNewsDto.description());
        NewsDTO updatedNews = newsService.updateNews(news);
        log.info("Новость с ID: {} успешно обновлена.", id);
        return updatedNews;
    }

    @Operation(
            summary = "Удаление объявления",
            description = "Позволяет администратору удалить существующее объявление по его ID.",
            responses = {
                    @ApiResponse(description = "Новость успешно удалена", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NewsDTO.class))),
                    @ApiResponse(description = "Новость не найдена", responseCode = "404")
            }
    )
    @DeleteMapping("/api/v1/admins/news/{id}")
    public NewsDTO deleteNews(@PathVariable Integer id) {
        log.info("Администратор запрашивает удаление новости ID: {}", id);
        News news = newsService.getNews(id);
        if (news == null) {
            log.warn("Новость с ID {} не найдена.", id);
            throw new NoSuchElementException("Нет такой новости!");
        }

        NewsDTO deletedNewsDTO = newsMapper.toNewsDTO(news);

        newsService.deleteNews(id);
        log.info("Новость с ID: {} успешно удалена.", id);
        return deletedNewsDTO;
    }

    @Operation(
            summary = "Блокирование пользователя",
            description = "Позволяет администратору заблокировать пользователя по его ID.",
            responses = {
                    @ApiResponse(description = "Пользователь успешно заблокирован", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = Users.class))),
                    @ApiResponse(description = "Нельзя заблокировать самого себя", responseCode = "400"),
                    @ApiResponse(description = "Пользователь не найден", responseCode = "404")
            }
    )
    @PutMapping("/api/v1/admins/users/{id}/blocked")
    public String blockedUser(@PathVariable Integer id) throws UserSelfException {
        log.info("Администратор запрашивает блокировку пользователя ID: {}", id);
        UserDTO userDTO = userService.blockUser(id);
        String username = userDTO.username();
        log.info("Пользователь с ID: {} успешно заблокирован.", id);
        return username;
    }

    @Operation(
            summary = "Разблокирование пользователя",
            description = "Позволяет администратору разблокировать пользователя по его ID.",
            responses = {
                    @ApiResponse(description = "Пользователь успешно разблокирован", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = Users.class))),
                    @ApiResponse(description = "Нельзя разблокировать самого себя", responseCode = "400"),
                    @ApiResponse(description = "Пользователь не найден", responseCode = "404")
            }
    )
    @PutMapping("/api/v1/admins/users/{id}/unblocked")
    public String unblockedUser(@PathVariable Integer id) throws UserSelfException {
        log.info("Администратор запрашивает разблокировку пользователя ID: {}", id);
        UserDTO userDTO = userService.unblockUser(id);
        String username = userDTO.username();
        log.info("Пользователь с ID: {} успешно разблокирован.", id);
        return username;
    }

    @Operation(
            summary = "Настройка лимита объявлений для пользователя",
            description = "Позволяет администратору установить лимит на количество объявлений, которые пользователь может создавать в день.",
            responses = {
                    @ApiResponse(description = "Лимит успешно установлен", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = Users.class))),
                    @ApiResponse(description = "Пользователь не найден", responseCode = "404")
            }
    )
    @PutMapping("/api/v1/admins/users/{id}/limits")
    public UserDTO userLimit(@PathVariable Integer id, @RequestBody Integer updateLimit) {
        log.info("Администратор устанавливает лимит на количество объявлений для пользователя ID: {}. Новый лимит: {}", id, updateLimit);
        UserDTO userDTO = userService.updateLimitNews(id, updateLimit);
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
    @GetMapping("/api/v1/admin/archives")
    public List<NewsDTO> getArchive() {
        log.info("Администратор запрашивает просмотр архива объявлений.");
        List<NewsDTO> archive = newsService.getArchive();
        log.info("Администратор получил {} архивных объявлений.", archive.size());
        return archive;
    }

    @Operation(
            summary = "Выдача роли пользователю",
            description = "Позволяет администратору выдать пользователю роль по его ID.",
            responses = {
                    @ApiResponse(description = "Роль успешно обновлена", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = UserDTO.class))),
                    @ApiResponse(description = "Пользователь не найден", responseCode = "404")
            }
    )
    @PutMapping("/api/v1/admins/users/{id}/role")
    public UserDTO updateUserRole(@PathVariable Integer id, @RequestBody RoleDTO roleDTO) {
        log.info("Администратор назначает новую роль для пользователя ID: {}. Новая роль: {}", id, roleDTO.role());
        UserDTO userDTO = userService.updateRole(id, roleDTO.role());
        log.info("Роль для пользователя ID: {} успешно обновлена на {}.", id, roleDTO.role());
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
    @GetMapping("/api/v1/admin/users")
    public List<UserDTO> getAllUser() {
        log.info("Администратор запрашивает список всех пользователей.");
        List<UserDTO> users = authServiceClient.getAllUsers();
        log.info("Администратор получил список из {} пользователей.", users.size());
        return users;
    }
}

