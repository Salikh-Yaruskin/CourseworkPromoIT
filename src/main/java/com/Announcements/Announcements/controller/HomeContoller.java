package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.MyException.BlockedException;
import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Status;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.service.EmailService;
import com.Announcements.Announcements.service.NewsService;
import com.Announcements.Announcements.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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


//    @GetMapping("/news/")
//    public List<News> getNewsUser(@RequestParam(required = false, defaultValue = "0") Integer userId) {
//       return newsService.getNewsUser(userId);
//   }

    // просмотр объявлений с отображнием количества просмотров
    @GetMapping("/news")
    public List<News> getAll() {
        return newsService.getAll();
    }

    @GetMapping("/news/{id}")
    public News getNews(@PathVariable Integer id) throws Exception{
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        News news = newsService.getNews(id, username);
        if(news.getStatus() == Status.BLOCKED){
            throw new BlockedException("Новость скрыта");
        }
        return newsService.getNews(id, username);
    }
}
