package com.Announcements.Announcements.service;

import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.repository.NewsRepository;
import com.Announcements.Announcements.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;
import java.util.Optional;

@Service
public class NewsService {
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private UserService userService;

    public NewsService(NewsRepository newsRepository, UserService userService) {
        this.newsRepository = newsRepository;
        this.userService = userService;
    }

    public List<News> getNewsUser(Integer newsId) {
        if (Objects.equals(newsId, 0L)) {
            return StreamSupport.stream(newsRepository.findAll().spliterator(), false).toList();
        }
        return StreamSupport.stream(newsRepository.findAllByUserId(newsId).spliterator(), false).toList();
    }

    public List<News> getAll() {
        return StreamSupport.stream(newsRepository.findAll().spliterator(), false).toList();
    }

    public News getNews(Integer id){
        Optional<News> news = newsRepository.findById(id);
        return news.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + news));
    }

    public News addNews(News news) {
        if (news == null) {
            throw new IllegalArgumentException("Entity is null");
        }
        return newsRepository.save(news);
    }
}
