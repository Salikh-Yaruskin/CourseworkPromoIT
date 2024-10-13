package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.service.NewsService;
import com.Announcements.Announcements.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private NewsService newsService;

    // редактирование объявления
    @PutMapping("/admin/all-news/{id}")
    public News updateAdminNews(@PathVariable Integer id, @RequestBody News updateNews){
        News news = newsService.getNews(id);
        if (news == null){
            throw new NoSuchElementException("Нет такой новости!");
        }

        news.setName(updateNews.getName());
        news.setDescription(updateNews.getDescription());

        return newsService.updateNews(news);
    }

    // удаление объявления
    @DeleteMapping("/admin/news/{id}")
    public News deleteNews(@PathVariable Integer id){
        News news = newsService.getNews(id);
        if(news == null){
            throw new NoSuchElementException("Нет такой новости!");
        }

        newsService.deleteNews(id);
        return news;
    }

    // блокирование клиента


    // настройка лимита объявлений клиентом в день

    // просмотр архива объявлений
    @GetMapping("/admin/news-archive")
    public List<News> getArchive(){
        return newsService.getArchive();
    }
}
