package com.Announcements.Announcements.service;

import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.UserView;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.repository.NewsRepository;
import com.Announcements.Announcements.repository.UserRepository;
import com.Announcements.Announcements.repository.UserViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.StreamSupport;
import java.util.Optional;

@Service
public class NewsService {
    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserViewRepository userViewRepository;

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

    public News getNews(Integer id, String username) {
        Optional<News> newsOptional = newsRepository.findById(id);

        if (!newsOptional.isPresent()) {
            throw new NoSuchElementException("News not found with id: " + id);
        }
        News news = newsOptional.get();

        Optional<Users> userOptional = userRepository.findByUsername(username);

        if (!userOptional.isPresent()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        Users user = userOptional.get();

        Optional<UserView> userViewOptional = userViewRepository.findByUserAndNews(user, news);

        if (!userViewOptional.isPresent()) {
            news.setViewCount(news.getViewCount() + 1);
            newsRepository.save(news);

            UserView userView = new UserView(user, news);
            userViewRepository.save(userView);
        }

        return news;
    }


    public News addNews(News news) {
        if (news == null) {
            throw new IllegalArgumentException("Entity is null");
        }
        return newsRepository.save(news);
    }
}
