package com.Announcements.Announcements.controller;

import com.Announcements.Announcements.MyException.UserSelfException;
import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.dto.RoleDTO;
import com.Announcements.Announcements.dto.UpdateNewsDTO;
import com.Announcements.Announcements.dto.UserDTO;
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
    public NewsDTO updateAdminNews(@PathVariable Integer id, @RequestBody UpdateNewsDTO updateNewsDto){
        News news = newsService.getNews(id);
        if (news == null){
            throw new NoSuchElementException("Нет такой новости!");
        }

        news.setName(updateNewsDto.name());
        news.setDescription(updateNewsDto.description());

        News updatedNews = newsService.updateNews(news);

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

    // удаление объявления
    @DeleteMapping("/admin/news/{id}")
    public NewsDTO deleteNews(@PathVariable Integer id) {
        News news = newsService.getNews(id);
        if (news == null) {
            throw new NoSuchElementException("Нет такой новости!");
        }

        NewsDTO deletedNewsDTO = new NewsDTO(
                news.getId(),
                news.getName(),
                news.getDescription(),
                news.getUser().getUsername(),
                news.getUser().getGmail(),
                news.getViewCount(),
                news.getStatus()
        );

        newsService.deleteNews(id);

        return deletedNewsDTO;
    }

    // блокирование клиента
    @PutMapping("/admin/user-blocked/{id}")
    public Users blockedUser(@PathVariable Integer id, @RequestBody Users blockedUser) throws UserSelfException {
        return userService.blockUser(id);
    }

    // разблокирование клиента
    @PutMapping("/admin/user-unblocked/{id}")
    public Users unblockedUser(@PathVariable Integer id, @RequestBody Users unBlockedUser) throws UserSelfException {
        return userService.unblockUser(id);
    }

    // настройка лимита объявлений клиентом в день
    @PutMapping("/admin/user-limit/{id}")
    public Users userLimit(@PathVariable Integer id, @RequestBody Integer updateLimit) {
        Users user = userService.findById(id);
        if (user == null){
            throw new NoSuchElementException("Нет такого пользователя!");
        }

        user.setLimitNews(updateLimit);
        return userService.updateUser(user);
    }

    // просмотр архива объявлений
    @GetMapping("/admin/news-archive")
    public List<NewsDTO> getArchive(){
        return newsService.getArchive();
    }

    // выдача роли пользователю
    @PutMapping("/admin/user-role/{id}")
    public UserDTO updateUserRole(@PathVariable Integer id, @RequestBody RoleDTO roleDTO){
        return userService.updateRole(id, roleDTO.role());
    }

    // выдача списка со всеми пользователями
    @GetMapping("/admin/all-user")
    public List<UserDTO> getAllUser(){
        return userService.getAllUser();
    }
}
