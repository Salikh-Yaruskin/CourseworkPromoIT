package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.service.EmailService;
import com.Announcements.Announcements.service.NewsService;
import com.Announcements.Announcements.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private EmailService emailService;

    // регистрация
    @PostMapping("/register")
    public Users register(@RequestBody Users user){
        return userService.register(user);
    }

    // вход
    @PostMapping("/login")
    public String login(@RequestBody Users user){
        return userService.verify(user);
    }

    // создание объявления
    @PostMapping("/user/create-news")
    public News addNews(@RequestBody News news) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userService.findByUsername(username);
        news.setUser(user);
        return newsService.addNews(news);
    }

    // ответ на объявление (отправкой email на почту автора объявления)
    @PostMapping("/user/news/{id}/send-email")
    public String sendEmailToAuthor(@PathVariable Integer id, @RequestBody String message) throws Exception{
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

    // просмотр своих объявлений с отображнием количества просмотров
    @GetMapping("/user/my-news")
    public List<News> getNewsUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = userService.findByUsername(username);
        if (user == null) {
            return List.of();
        }
        return newsService.getNewsByUserId(user.getId());
    }

    // закрытие объявления его автором
    @PutMapping("/user/my-news/status/{id}")
    public News updateStatusNews(@PathVariable Integer id, @RequestBody News updatedNews) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userService.findByUsername(username);

        News existingNews = newsService.getNews(id, username);

        if (existingNews == null || !Objects.equals(existingNews.getUser().getId(), user.getId())) {
            throw new IllegalArgumentException("Вы не можете редактировать эту новость.");
        }

        existingNews.setStatus(updatedNews.getStatus());

        return newsService.updateNews(existingNews);
    }
}
