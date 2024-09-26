package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.model.News;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class HomeContoller {

    private List<News> news = new ArrayList<>(
            List.of(
                    new News("Navin", "60"),
                    new News("Kiran", "65")
            ));

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

    @PostMapping("admin/news")
    public News addNews(@RequestBody News newss){
        news.add(newss);
        return newss;
    }

    @GetMapping("/news")
    public List<News> getNews(){
        return news;
    }
}
