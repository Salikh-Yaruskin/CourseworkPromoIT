package com.Announcements.Announcements.service;

import com.Announcements.Announcements.MyException.BlockedException;
import com.Announcements.Announcements.MyException.UnlimitedException;
import com.Announcements.Announcements.dto.CreateNewsDTO;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserViewRepository userViewRepository;
    private final UserMapper userMapper;
    private final NewsMapper newsMapper;

    public List<NewsDTO> getNewsUser(Integer newsId) {
        if (Objects.equals(newsId, 0L)) {
            List<News> list = newsRepository.findAll();
            log.info("Получены все новости ({} записей)", list.size());
            return newsMapper.toNewsDTOList(list);
        }
        List<News> list_id = newsRepository.findAllByUserId(newsId);
        return newsMapper.toNewsDTOList(list_id);
    }

    public List<NewsDTO> getAll() {
        List<News> news = newsRepository.findAll();
        List<News> ansNews = new ArrayList<>();
        for (News a : news){
            if(a.getStatus() == Status.UNBLOCKED){
                ansNews.add(a);
            }
        }
        return newsMapper.toNewsDTOList(ansNews);
    }

    public List<NewsDTO> getArchive(){
        List<News> news = newsRepository.findAll();
        List<News> archiveNews = new ArrayList<>();
        for (News a : news){
            if (a.getStatus() == Status.BLOCKED){
                archiveNews.add(a);
            }
        }
        return newsMapper.toNewsDTOList(archiveNews);
    }

    public NewsDTO getSimpleNews(Integer id, String username) throws Exception{
        Optional<News> newsOptional = newsRepository.findById(id);

        News news = newsRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Новость с ID {} не найдена.", id);
                    return new NoSuchElementException("News not found with id: " + id);
                });

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь {} не найден.", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        Optional<Users> userOptional = userRepository.findByUsername(username);

        log.info("Новость с ID: {} успешно найдена.", id);
        return newsMapper.toNewsDTO(news);
    }

    public NewsDTO getNews(Integer id, String username) throws Exception{
        Optional<News> newsOptional = newsRepository.findById(id);

        News news = newsRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Новость с ID {} не найдена.", id);
                    return new NoSuchElementException("News not found with id: " + id);
                });

        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь {} не найден.", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        Optional<UserView> userViewOptional = userViewRepository.findByUserAndNews(user, news);

        if (userViewOptional.isEmpty()) {
            news.setViewCount(news.getViewCount() + 1);
            newsRepository.save(news);

            UserView userView = new UserView(user, news);
            userViewRepository.save(userView);
            log.info("Просмотр новости с ID {} зарегистрирован для пользователя: {}", id, username);
        }
        return newsMapper.toNewsDTO(news);
    }

    public News getNews(Integer id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Not found news with id: " + id));
    }

    public List<NewsDTO> getNewsByUserId(Integer userId) {
        List<News> news = newsRepository.findAllByUserId(userId);
        return newsMapper.toNewsDTOList(news);
    }

    @Transactional
    public NewsDTO addNews(News news) throws Exception {
        if (news == null) {
            log.error("Попытка добавить пустую новость.");
            throw new IllegalArgumentException("Entity is null");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO user = userService.findByUsername(username);

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX);

        List<News> todayAnnouncements = newsRepository.findAllByUserIdAndCreatedAtBetween(userMapper.fromUserDto(user).getId(), startOfDay, endOfDay);
        int todayAnnouncementsCount = todayAnnouncements.size();

        if (todayAnnouncementsCount >= userMapper.fromUserDto(user).getLimitNews()) {
            log.warn("Превышен лимит новостей для пользователя {}: {}", username, userMapper.fromUserDto(user).getLimitNews());
            throw new UnlimitedException("Вы не можете публиковать больше " + userMapper.fromUserDto(user).getLimitNews() + " объявлений в день");
        }

        userService.checkBlocking(userMapper.fromUserDto(user).getId());

        news.setUser(userMapper.fromUserDto(user));
        news.setCreatedAt(LocalDateTime.now());
        newsRepository.save(news);
        return newsMapper.toNewsDTO(news);
    }

    public NewsDTO updateNews(Integer id, Status status) throws Exception {

        News news = newsRepository.findById(id)
                .orElseThrow(() ->{
                    log.warn("Новость с ID {} не найдена.", id);
                    return new NoSuchElementException("News not found with id: " + id);
                });
        news.setStatus(status);
        newsRepository.save(news);
        return newsMapper.toNewsDTO(news);
    }

    public NewsDTO updateNews(News news) {
        if (news == null || news.getId() == 0) {
            throw new IllegalArgumentException("Новость не найдена или некорректна.");
        }
        return newsMapper.toNewsDTO(news);
    }

    public void deleteNews(Integer id){

        News news = newsRepository.findById(id)
                .orElseThrow(() -> {
                   log.warn("Новость с ID {} не найдена.", id);
                   return new NoSuchElementException("News not found with id: " + id);
                });

        List<UserView> userViews = userViewRepository.findAllByNews(news);
        userViewRepository.deleteAll(userViews);

        newsRepository.deleteById(id);
    }

    public Page<NewsDTO> findNewsWithPagination(int offset, int size){
        Pageable pageable = PageRequest.of(offset, size);

        Page<News> newsPage = newsRepository.findAll(pageable);

        List<News> filteredNewsList = newsPage.stream()
                .filter(news -> news.getStatus() != Status.BLOCKED)
                .toList();

        return new PageImpl<>(newsMapper.toNewsDTOList(filteredNewsList), pageable, filteredNewsList.size());
    }
}
