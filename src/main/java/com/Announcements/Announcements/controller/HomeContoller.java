package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.service.NewsService;
import com.Announcements.Announcements.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class HomeContoller {
    @Autowired
    private NewsService newsService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(){
        return  "Hello World!";
    }

    @GetMapping("/home")
    public String handleWelcome() {
        return "Welcome to home!";
    }

    @GetMapping("/admin/home")
    public String handleAdminHome() {
        return "Welcome to ADMIN home!";
    }

    @GetMapping("/user/home")
    public String handleUserHome() {
        return "Welcome to USER home!";
    }

//    @GetMapping("/news/")
//    public List<News> getNewsUser(@RequestParam(required = false, defaultValue = "0") Integer userId) {
//        return newsService.getNewsUser(userId);
//    }

    @GetMapping("/news")
    public List<News> getAll() {
        return newsService.getAll();
    }

    @GetMapping("/news/{id}")
    public News getNews(@PathVariable Integer id){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return  newsService.getNews(id, username);
    }

    @PostMapping("/admin/news")
    public News addNews(@RequestBody News news) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = userService.findByUsername(username);
        news.setUser(user);
        return newsService.addNews(news);
    }
}
