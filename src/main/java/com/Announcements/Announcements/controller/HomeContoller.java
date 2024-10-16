package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.MyException.BlockedException;
import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Status;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.service.EmailService;
import com.Announcements.Announcements.service.NewsService;
import com.Announcements.Announcements.service.UserService;
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

    // просмотр объявлений с отображнием количества просмотров
    @GetMapping("/news")
    public List<NewsDTO> getAll() {
        return newsService.getAll();
    }

    // просмотр объявлений с пагинацией
    @GetMapping("/news/{page}/{size}")
    public Page<News> getAllWithPagination(@PathVariable Integer page, @PathVariable Integer size) {
        return newsService.findNewsWithPagination(page, size);
    }

    // получение объявления по id
    @GetMapping("/news/{id}")
    public NewsDTO getNews(@PathVariable Integer id) throws Exception{
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        NewsDTO newsDTO = newsService.getNews(id, username);
        if(newsDTO.status() == Status.BLOCKED){
            throw new BlockedException("Новость скрыта");
        }
        return newsService.getNews(id, username);
    }
}
