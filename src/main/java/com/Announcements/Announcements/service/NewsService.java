package com.Announcements.Announcements.service;

import com.Announcements.Announcements.MyException.BlockedException;
import com.Announcements.Announcements.MyException.UnlimitedException;
import com.Announcements.Announcements.dto.CreateNewsDTO;
import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.dto.UpdateNewsDTO;
import com.Announcements.Announcements.dto.UserDTO;
import com.Announcements.Announcements.mapper.NewsMapper;
import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Status;
import com.Announcements.Announcements.model.UserView;
import com.Announcements.Announcements.repository.NewsRepository;
import com.Announcements.Announcements.repository.UserViewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final UserViewRepository userViewRepository;
    private final NewsMapper newsMapper;
    private final AuthServiceClient authServiceClient;

    // OK
    public List<NewsDTO> getAll() {
        log.info("Получение всех новостей");
        List<News> news = newsRepository.findAll();
        List<News> ansNews = new ArrayList<>();
        for (News a : news) {
            if (a.getStatus() == Status.UNBLOCKED) {
                ansNews.add(a);
            }
        }
        return newsMapper.toNewsDTOList(ansNews);
    }

    // OK
    public List<NewsDTO> getArchive() {
        log.info("Получение объявлений со статусов BLOCKED");
        List<News> news = newsRepository.findAll();
        List<News> archiveNews = new ArrayList<>();
        for (News a : news) {
            if (a.getStatus() == Status.BLOCKED) {
                archiveNews.add(a);
            }
        }
        return newsMapper.toNewsDTOList(archiveNews);
    }

    // OK
    public NewsDTO getSimpleNews(Integer id) throws Exception {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Новость с ID {} не найдена.", id);
                    return new NoSuchElementException("Новость не найдена с ID: " + id);
                });

        log.info("Новость с ID: {} успешно найдена.", id);
        return newsMapper.toNewsDTO(news);
    }

    // OK
    public NewsDTO getNews(Integer id, String username) throws Exception {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Новость с ID {} не найдена.", id);
                    return new NoSuchElementException("Новость не найдена с ID: " + id);
                });

        if (!username.isEmpty()) {
            authServiceClient.getUserByName(username)
                    .ifPresent(user -> {
                        Optional<UserView> userViewOptional = userViewRepository.findByUserAndNews(user.id(), news);

                        if (userViewOptional.isEmpty()) {
                            news.setViewCount(news.getViewCount() + 1);
                            newsRepository.save(news);

                            UserView userView = new UserView(user.id(), news);
                            userViewRepository.save(userView);
                            log.info("Просмотр новости с ID {} зарегистрирован для пользователя: {}", id, username);
                        }
                    });
        }
        log.info("Новость с ID: {} успешно найдена.", id);
        return newsMapper.toNewsDTO(news);
    }

    public NewsDTO getNews(Integer id) {
        return newsMapper.toNewsDTO(newsRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Новость не найдена с ID: " + id)));
    }

    public List<NewsDTO> getNewsByUserId(Integer userId) {
        log.info("Получение объявлений пользователя с ID: {}", userId);
        List<News> news = newsRepository.findAllByUser(userId);
        return newsMapper.toNewsDTOList(news);
    }

    @Transactional
    public NewsDTO addNews(CreateNewsDTO newsDto) throws Exception {
        log.info("Добавление новости: {}", newsDto.name());
        if (newsDto == null) {
            log.error("Попытка добавить пустую новость.");
            throw new IllegalArgumentException("Entity is null");
        }

        News news = new News(newsDto);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<UserDTO> userOpt = authServiceClient.getUserByName(username);
        UserDTO user = userOpt.get();

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX);

        List<News> todayAnnouncements = newsRepository.findAllByUserIdAndCreatedAtBetween(user.id(), startOfDay, endOfDay);
        int todayAnnouncementsCount = todayAnnouncements.size();

        if (todayAnnouncementsCount >= user.limitNews()) {
            log.warn("Превышен лимит новостей для пользователя {}: {}", username, user.limitNews());
            throw new UnlimitedException("Вы не можете публиковать больше " + user.limitNews() + " объявлений в день");
        }

        try {
            authServiceClient.checkUser(user.id());
        } catch (Exception e) {
            throw new BlockedException(e.getMessage());
        }


        news.setUser(user.id());
        news.setCreatedAt(LocalDateTime.now());
        newsRepository.save(news);
        log.info("Новость успешно добавлена: {}", newsDto.name());
        return newsMapper.toNewsDTO(news);
    }

    public NewsDTO updateNews(Integer id, Status status) throws Exception {
        log.info("Обновление новости ID: {} с новым статусом: {}", id, status);
        News news = newsRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Новость с ID {} не найдена.", id);
                    return new NoSuchElementException("News not found with id: " + id);
                });
        news.setStatus(status);
        newsRepository.save(news);
        log.info("Статус новости {} успешно изменен на {}", id, status);
        return newsMapper.toNewsDTO(news);
    }

    public NewsDTO updateNews(UpdateNewsDTO newNews, Integer id) throws Exception {
        log.info("Обновление новости ID: {} с новыми данными: {}", id, newNews);
        Optional<News> newsOptional = newsRepository.findById(id);
        if (newsOptional.isEmpty()) {
            log.warn("Новость с ID {} не найдена.", id);
            throw new NoSuchElementException("Нет такой новости!");
        }
        log.info("Обновление новости ID: {} с новыми данными: {}", id, newNews);
        News news = newsOptional.get();
        news.setName(newNews.name());
        news.setDescription(newNews.description());
        newsRepository.save(news);
        log.info("Новость с ID: {} успешно обновлена.", id);
        return newsMapper.toNewsDTO(news);
    }

    public NewsDTO deleteNews(Integer id) {
        log.info("Удаление новости ID: {}", id);
        News news = newsRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Новость с ID {} не найдена.", id);
                    return new NoSuchElementException("Новость не найдена с ID: " + id);
                });

        List<UserView> userViews = userViewRepository.findAllByNews(news);
        userViewRepository.deleteAll(userViews);
        newsRepository.deleteById(id);
        log.info("Новость с ID: {} успешно удалена.", id);
        return newsMapper.toNewsDTO(news);
    }

    public Page<NewsDTO> findNewsWithPagination(int offset, int size) {
        log.info("Получение объявлений с пагинацией");
        Pageable pageable = PageRequest.of(offset, size);

        Page<News> newsPage = newsRepository.findAll(pageable);

        List<News> filteredNewsList = newsPage.stream()
                .filter(news -> news.getStatus() != Status.BLOCKED)
                .toList();

        return new PageImpl<>(newsMapper.toNewsDTOList(filteredNewsList), pageable, filteredNewsList.size());
    }
}
