package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.MyException.BlockedException;
import com.Announcements.Announcements.dto.LoginDTO;
import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.dto.UserCreateDTO;
import com.Announcements.Announcements.dto.UserDTO;
import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Status;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.service.EmailService;
import com.Announcements.Announcements.service.NewsService;
import com.Announcements.Announcements.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HomeContoller {

    private final NewsService newsService;
    private final UserService userService;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Позволяет зарегистрировать нового пользователя в системе",
            responses = {
                    @ApiResponse(description = "Успешная регистрация", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = UserDTO.class))),
                    @ApiResponse(description = "Ошибка валидации или регистрация не удалась", responseCode = "400")
            }
    )

    @PostMapping("/api/v1/register")
    public UserCreateDTO register(@RequestBody UserCreateDTO userCreateDTO){
        log.info("Регистрация пользователя {}", userCreateDTO.username());
        UserCreateDTO createUser = userService.register(userCreateDTO);
        log.info("Пользователь успешно зарегистрировался: {}", createUser.username());
        return createUser;
    }

    @Operation(
            summary = "Авторизация пользователя",
            description = "Позволяет авторизовать пользователя по логину и паролю",
            responses = {
                    @ApiResponse(description = "Успешная авторизация", responseCode = "200"),
                    @ApiResponse(description = "Неверные данные авторизации", responseCode = "401")
            }
    )
    @PostMapping("/api/v1/login")
    public String login(@RequestBody LoginDTO loginDTO){
        log.info("Попытка входа пользователем {}", loginDTO.username());
        String token = userService.verify(loginDTO);
        log.info("Пользователь {} вошел успешно", loginDTO.username());
        return token;
    }

    @Operation(
            summary = "Получение всех объявлений",
            description = "Позволяет просмотреть все доступные объявления с отображением количества просмотров.",
            responses = {
                    @ApiResponse(description = "Список объявлений успешно получен", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NewsDTO.class)))
            }
    )
    @GetMapping("/api/v1/news")
    public List<NewsDTO> getAll() {
        log.info("Получение списка всех объявлений");
        List<NewsDTO> listNews = newsService.getAll();
        log.info("Количество полученных объявлений: {}", listNews.size());
        return listNews;
    }

    @Operation(
            summary = "Получение объявлений с пагинацией",
            description = "Позволяет просмотреть объявления с постраничной навигацией. Укажите номер страницы и количество объявлений на страницу.",
            parameters = {
                    @Parameter(name = "page", description = "Номер страницы", example = "1"),
                    @Parameter(name = "size", description = "Количество объявлений на странице", example = "10")
            },
            responses = {
                    @ApiResponse(description = "Объявления с пагинацией успешно получены", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = Page.class))),
                    @ApiResponse(description = "Некорректные параметры пагинации", responseCode = "400")
            }
    )

    @GetMapping("/api/v1/news/{page}/{size}")
    public Page<NewsDTO> getAllWithPagination(@PathVariable Integer page, @PathVariable Integer size) {
        log.info("Получения спика новосетей согласно пагинации: page={}, size={}", page, size);
        return newsService.findNewsWithPagination(page, size);
    }

    @Operation(
            summary = "Получение объявления по ID",
            description = "Позволяет получить конкретное объявление по его ID. Если новость заблокирована, будет выброшено исключение.",
            parameters = {
                    @Parameter(name = "id", description = "ID объявления", example = "123")
            },
            responses = {
                    @ApiResponse(description = "Объявление успешно получено", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NewsDTO.class))),
                    @ApiResponse(description = "Объявление заблокировано", responseCode = "403",
                            content = @Content(schema = @Schema(implementation = BlockedException.class))),
                    @ApiResponse(description = "Объявление не найдено", responseCode = "404")
            }
    )
    @GetMapping("/api/v1/news/{id}")
    public NewsDTO getNews(@PathVariable Integer id) throws Exception {
        log.info("Неавторизованый пользователь хочет получить новость id={}", id);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        NewsDTO newsDTO = newsService.getSimpleNews(id, username);
        if(newsDTO.status() == Status.BLOCKED) {
            throw new BlockedException("Новость скрыта");
        }
        NewsDTO newsGetDTO = newsService.getNews(id, username);
        log.info("Неавторизованный пользователь получает новость id={}", id);
        return newsGetDTO;
    }
}

