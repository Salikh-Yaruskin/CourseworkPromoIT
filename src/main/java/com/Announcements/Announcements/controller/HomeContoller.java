package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.service.EmailService;
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

    @Autowired
    private EmailService emailService;

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

    @PostMapping("/news/{id}/send-email")
    public String sendEmailToAuthor(@PathVariable Integer id, @RequestBody String message){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        News news = newsService.getNews(id, username);
        if (news == null) {
            return "Новость не найдена";
        }

        Users author = news.getUser();
        if (author == null || author.getGmail() == null) {
            return "Автор не верный или почта не верна!";
        }

        if(author.getUsername().equals(username)){
            return "Вы не можете комментировать свои новости!";
        }

        emailService.sendSimpleEmail(
                author.getGmail(),
                "Комментарий от пользователя!",
                message,
                author.getUsername()
        );

        return "Письмо отправлено: " + author.getUsername();
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
