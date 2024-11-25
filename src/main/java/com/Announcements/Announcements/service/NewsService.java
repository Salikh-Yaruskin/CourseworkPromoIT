package com.Announcements.Announcements.service;

import com.Announcements.Announcements.MyException.BlockedException;
import com.Announcements.Announcements.MyException.UnlimitedException;
import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.dto.UserDTO;
import com.Announcements.Announcements.mapper.NewsMapper;
import com.Announcements.Announcements.mapper.UserMapper;
import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Status;
import com.Announcements.Announcements.model.UserView;
import com.Announcements.Announcements.model.Users;
import com.Announcements.Announcements.repository.NewsRepository;
import com.Announcements.Announcements.repository.UserRepository;
import com.Announcements.Announcements.repository.UserViewRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
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

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NewsMapper newsMapper;

    public NewsService(NewsRepository newsRepository, UserService userService) {
        this.newsRepository = newsRepository;
        this.userService = userService;
    }

    public List<NewsDTO> getNewsUser(Integer newsId) {
        if (Objects.equals(newsId, 0L)) {
            List<News> list = StreamSupport.stream(newsRepository.findAll().spliterator(), false).toList();
            return newsMapper.toNewsDTOList(list);
        }
        List<News> list_id = StreamSupport.stream(newsRepository.findAllByUserId(newsId).spliterator(), false).toList();
        return newsMapper.toNewsDTOList(list_id);
    }

    public List<NewsDTO> getAll() {
        List<News> news = StreamSupport.stream(newsRepository.findAll().spliterator(), false).toList();
        List<News> ansNews = new ArrayList<>();
        for (News a : news){
            if(a.getStatus() == Status.UNBLOCKED){
                ansNews.add(a);
            }
        }
        return newsMapper.toNewsDTOList(ansNews);
    }

    public List<NewsDTO> getArchive(){
        List<News> news = StreamSupport.stream(newsRepository.findAll().spliterator(), false).toList();
        List<News> archiveNews = new ArrayList<>();
        for (News a : news){
            if (a.getStatus() == Status.BLOCKED){
                archiveNews.add(a);
            }
        }
        return newsMapper.toNewsDTOList(archiveNews);
    }

    public NewsDTO getNews(Integer id, String username) throws Exception{
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
        return newsMapper.toNewsDTO(news);
    }

    public News getNews(Integer id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Not found news with id: " + id));
    }

    public List<NewsDTO> getNewsByUserId(Integer userId) {
        List<News> news = StreamSupport.stream(newsRepository.findAllByUserId(userId).spliterator(), false).toList();
        return newsMapper.toNewsDTOList(news);
    }

    @Transactional
    public NewsDTO addNews(News news) throws Exception {
        if (news == null) {
            throw new IllegalArgumentException("Entity is null");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO user = userService.findByUsername(username);

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX);

        List<News> todayAnnouncements = newsRepository.findAllByUserIdAndCreatedAtBetween(userMapper.fromUserDto(user).getId(), startOfDay, endOfDay);
        int todayAnnouncementsCount = todayAnnouncements.size();

        if (todayAnnouncementsCount >= userMapper.fromUserDto(user).getLimitNews()) {
            throw new UnlimitedException("Вы не можете публиковать больше " + userMapper.fromUserDto(user).getLimitNews() + " объявлений в день");
        }

        userService.checkBlocking(userMapper.fromUserDto(user).getId());

        news.setUser(userMapper.fromUserDto(user));
        news.setCreatedAt(LocalDateTime.now());
        newsRepository.save(news);
        return newsMapper.toNewsDTO(news);
    }

    public NewsDTO updateNews(Integer id, Status status) throws Exception {
        Optional<News> newsOptional = newsRepository.findById(id);
        if (!newsOptional.isPresent()) {
            throw new NoSuchElementException("News not found with id: " + id);
        }
        News news = newsOptional.get();
        news.setStatus(status);
        newsRepository.save(news);
        return newsMapper.toNewsDTO(news);
    }

    public NewsDTO updateNews(News news) {
        if (news == null || news.getId() == null) {
            throw new IllegalArgumentException("Новость не найдена или некорректна.");
        }
        return newsMapper.toNewsDTO(news);
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

    public Page<NewsDTO> findNewsWithPagination(int offset, int size){
        Page<News> newsPage = newsRepository.findAll(PageRequest.of(offset, size));
        List<News> filteredNewsList = newsPage.stream()
                .filter(news -> news.getStatus() != Status.BLOCKED).toList();

        return new PageImpl<>(newsMapper.toNewsDTOList(filteredNewsList), PageRequest.of(offset, size), newsMapper.toNewsDTOList(filteredNewsList).size());
    }
}
