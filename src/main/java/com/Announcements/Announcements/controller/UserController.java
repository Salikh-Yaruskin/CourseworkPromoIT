package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.dto.CreateNewsDTO;
import com.Announcements.Announcements.dto.LoginDTO;
import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.dto.UserDTO;
import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Status;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.service.CaptchaService;
import com.Announcements.Announcements.service.EmailService;
import com.Announcements.Announcements.service.NewsService;
import com.Announcements.Announcements.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Autowired
    private CaptchaService captchaService;

    // регистрация
    @PostMapping("/register")
    public UserDTO register(@RequestBody UserDTO userDTO){
        return userService.register(userDTO);
    }

    // вход
    @PostMapping("/login")
    public String login(@RequestBody LoginDTO loginDTO){
        return userService.verify(loginDTO);
    }

    // метод создания объявления с Captcha
    @PostMapping("/user/create-news")
    public NewsDTO addNews(@RequestBody CreateNewsDTO newsDto, @RequestHeader("g-recaptcha-response") String captchaResponse) throws Exception {
        if (!captchaService.validateCaptcha(captchaResponse)) {
            throw new Exception("Captcha validation failed.");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userService.findByUsername(username);

        News news = new News(newsDto);
        news.setUser(user);
        News createdNews = newsService.addNews(news);

        return new NewsDTO(
                createdNews.getId(),
                createdNews.getName(),
                createdNews.getDescription(),
                createdNews.getUser().getUsername(),
                createdNews.getUser().getGmail(),
                createdNews.getViewCount(),
                createdNews.getStatus()
        );
    }

    // ответ на объявление (отправкой email на почту автора объявления)
    @PostMapping("/user/news/{id}/send-email")
    public String sendEmailToAuthor(@PathVariable Integer id, @RequestBody String message) throws Exception{
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        NewsDTO newsDto  = newsService.getNews(id, username);
        if (newsDto  == null) {
            return "Новость не найдена";
        }

        String author = newsDto.username();
        String gmail = newsDto.gmail();
        if (author == null || gmail == null) {
            return "Автор не верный или почта не верна!";
        }

        if(newsDto.username().equals(username)){
            return "Вы не можете комментировать свои новости!";
        }

        emailService.sendSimpleEmail(
                gmail,
                "Комментарий от пользователя!",
                message,
                username
        );

        return "Письмо отправлено: " + username;
    }

    // просмотр своих объявлений с отображнием количества просмотров
    @GetMapping("/user/my-news")
    public List<NewsDTO> getNewsUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Users user = userService.findByUsername(username);
        if (user == null) {
            return List.of();
        }
        return newsService.getNewsByUserId(user.getId());
    }

    // закрытие объявления его автором
    @PutMapping("/user/my-news/status/{id}")
    public NewsDTO updateStatusNews(@PathVariable Integer id, @RequestBody Status updatedNewsStatus) throws Exception {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userService.findByUsername(username);

        NewsDTO existingNews = newsService.getNews(id, username);

        if (existingNews == null || !Objects.equals(existingNews.username(), user.getUsername())) {
            throw new IllegalArgumentException("Вы не можете редактировать эту новость.");
        }

        News updatedNews = newsService.updateNews(id, updatedNewsStatus);
        return new NewsDTO(
                updatedNews.getId(),
                updatedNews.getName(),
                updatedNews.getDescription(),
                updatedNews.getUser().getUsername(),
                updatedNews.getUser().getGmail(),
                updatedNews.getViewCount(),
                updatedNews.getStatus()
        );
    }
}
