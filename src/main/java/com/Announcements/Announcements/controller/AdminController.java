package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.service.NewsService;
import com.Announcements.Announcements.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private NewsService newsService;

    @PutMapping("/admin/news/{id}")
    public News updateNews(@PathVariable Integer id, @RequestBody News updateNews){
        News news = newsService.getNews(id);
        if (news == null){
            throw new NoSuchElementException("Нет такой новости!");
        }

        news.setName(updateNews.getName());
        news.setDescription(updateNews.getDescription());

        return newsService.updateNews(news);
    }

    @DeleteMapping("/admin/news/{id}")
    public News deleteNews(@PathVariable Integer id){
        News news = newsService.getNews(id);
        if(news == null){
            throw new NoSuchElementException("Нет такой новости!");
        }

        newsService.deleteNews(id);
        return news;
    }
}
