package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.MyException.UserSelfException;
import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.dto.RoleDTO;
import com.Announcements.Announcements.dto.UpdateNewsDTO;
import com.Announcements.Announcements.dto.UserDTO;
import com.Announcements.Announcements.mapper.NewsMapper;
import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.service.NewsService;
import com.Announcements.Announcements.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final NewsService newsService;
    private final NewsMapper newsMapper;

    @Operation(
            summary = "Редактирование объявления",
            description = "Позволяет администратору редактировать существующее объявление по его ID.",
            responses = {
                    @ApiResponse(description = "Новость успешно обновлена", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NewsDTO.class))),
                    @ApiResponse(description = "Новость не найдена", responseCode = "404")
            }
    )
    @PutMapping("/admin/all-news/{id}")
    public NewsDTO updateAdminNews(@PathVariable Integer id, @RequestBody UpdateNewsDTO updateNewsDto) {
        News news = newsService.getNews(id);
        if (news == null) {
            throw new NoSuchElementException("Нет такой новости!");
        }

        news.setName(updateNewsDto.name());
        news.setDescription(updateNewsDto.description());

        return newsService.updateNews(news);
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
    @DeleteMapping("/admin/news/{id}")
    public NewsDTO deleteNews(@PathVariable Integer id) {
        News news = newsService.getNews(id);
        if (news == null) {
            throw new NoSuchElementException("Нет такой новости!");
        }

        NewsDTO deletedNewsDTO = newsMapper.toNewsDTO(news);

        newsService.deleteNews(id);

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
    @PutMapping("/admin/user-blocked/{id}")
    public String blockedUser(@PathVariable Integer id) throws UserSelfException {
        UserDTO userDTO = userService.blockUser(id);
        return userDTO.username();
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
    @PutMapping("/admin/user-unblocked/{id}")
    public String unblockedUser(@PathVariable Integer id) throws UserSelfException {
        UserDTO userDTO = userService.unblockUser(id);
        return userDTO.username();
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
    @PutMapping("/admin/user-limit/{id}")
    public UserDTO userLimit(@PathVariable Integer id, @RequestBody Integer updateLimit) {
        return userService.updateLimitNews(id, updateLimit);
    }

    @Operation(
            summary = "Просмотр архива объявлений",
            description = "Позволяет администратору просмотреть архив всех объявлений.",
            responses = {
                    @ApiResponse(description = "Архив объявлений успешно получен", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NewsDTO.class)))
            }
    )
    @GetMapping("/admin/news-archive")
    public List<NewsDTO> getArchive() {
        return newsService.getArchive();
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
    @PutMapping("/admin/user-role/{id}")
    public UserDTO updateUserRole(@PathVariable Integer id, @RequestBody RoleDTO roleDTO) {
        return userService.updateRole(id, roleDTO.role());
    }

    @Operation(
            summary = "Получение списка всех пользователей",
            description = "Позволяет администратору получить список всех зарегистрированных пользователей.",
            responses = {
                    @ApiResponse(description = "Список пользователей успешно получен", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = UserDTO.class)))
            }
    )
    @GetMapping("/admin/all-user")
    public List<UserDTO> getAllUser() {
        return userService.getAllUser();
    }
}

