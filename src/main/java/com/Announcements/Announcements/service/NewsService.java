package com.Announcements.Announcements.service;

import com.Announcements.Announcements.MyException.BlockedException;
import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Status;
import com.Announcements.Announcements.model.UserView;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.repository.NewsRepository;
import com.Announcements.Announcements.repository.UserRepository;
import com.Announcements.Announcements.repository.UserViewRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.StreamSupport;

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
        List<News> news = StreamSupport.stream(newsRepository.findAll().spliterator(), false).toList();
        List<News> ansNews = new ArrayList<>();
        for (News a : news){
            if(a.getStatus() == Status.UNBLOCKED){
                ansNews.add(a);
            }
        }
        return ansNews;
    }

    public List<News> getArchive(){
        List<News> news = StreamSupport.stream(newsRepository.findAll().spliterator(), false).toList();
        List<News> archiveNews = new ArrayList<>();
        for (News a : news){
            if (a.getStatus() == Status.BLOCKED){
                archiveNews.add(a);
            }
        }
        return archiveNews;
    }

    public News getNews(Integer id, String username) throws Exception{
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

    public News getNews(Integer id){
        Optional<News> newsOptional = newsRepository.findById(id);
        if(!newsOptional.isPresent()){
            throw new NoSuchElementException("Not found news with id: " + id);
        }
        News news = newsOptional.get();

        return news;
    }

    public List<News> getNewsByUserId(Integer userId) {
        return StreamSupport.stream(newsRepository.findAllByUserId(userId).spliterator(), false).toList();
    }

    @Transactional
    public News addNews(News news) throws Exception {
        if (news == null) {
            throw new IllegalArgumentException("Entity is null");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userService.findByUsername(username);

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX);

        List<News> todayAnnouncements = newsRepository.findAllByUserIdAndCreatedAtBetween(user.getId(), startOfDay, endOfDay);
        int todayAnnouncementsCount = todayAnnouncements.size();

        if (todayAnnouncementsCount >= user.getLimitNews()) {
            throw new Exception("Вы не можете публиковать больше " + user.getLimitNews() + " объявлений в день");
        }

        userService.checkBlocking(user.getId());

        System.out.println(todayAnnouncementsCount);
        news.setUser(user);
        news.setCreatedAt(LocalDateTime.now());
        return newsRepository.save(news);
    }

    public News updateNews(News news) {
        if (news == null || news.getId() == null) {
            throw new IllegalArgumentException("Новость не найдена или некорректна.");
        }
        return newsRepository.save(news);
    }

    public void deleteNews(Integer id){
        Optional<News> newsOptional = newsRepository.findById(id);
        if (!newsOptional.isPresent()) {
            throw new NoSuchElementException("News not found with id: " + id);
        }
        News news = newsOptional.get();

        List<UserView> userViews = userViewRepository.findAllByNews(news);
        userViewRepository.deleteAll(userViews);

        newsRepository.deleteById(id);
    }
}
