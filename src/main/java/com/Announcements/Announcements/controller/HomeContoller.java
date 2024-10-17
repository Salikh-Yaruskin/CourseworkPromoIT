package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.MyException.BlockedException;
import com.Announcements.Announcements.dto.NewsDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
public class HomeContoller {

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserService userService;

    @Operation(
            summary = "Получение всех объявлений",
            description = "Позволяет просмотреть все доступные объявления с отображением количества просмотров.",
            responses = {
                    @ApiResponse(description = "Список объявлений успешно получен", responseCode = "200",
                            content = @Content(schema = @Schema(implementation = NewsDTO.class)))
            }
    )
    @GetMapping("/news")
    public List<NewsDTO> getAll() {
        return newsService.getAll();
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
    @GetMapping("/news/{page}/{size}")
    public Page<NewsDTO> getAllWithPagination(@PathVariable Integer page, @PathVariable Integer size) {
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
    @GetMapping("/news/{id}")
    public NewsDTO getNews(@PathVariable Integer id) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        NewsDTO newsDTO = newsService.getNews(id, username);
        if(newsDTO.status() == Status.BLOCKED) {
            throw new BlockedException("Новость скрыта");
        }
        return newsService.getNews(id, username);
    }
}

