package com.Announcements.Announcements.repository;

import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.UserView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserViewRepository extends JpaRepository<UserView, Integer> {

    Optional<UserView> findByUserAndNews(Integer user, News news);

    List<UserView> findAllByNews(News news);
}
