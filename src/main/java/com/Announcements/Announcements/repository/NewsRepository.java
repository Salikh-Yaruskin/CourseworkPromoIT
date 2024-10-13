package com.Announcements.Announcements.repository;

import com.Announcements.Announcements.model.News;
import com.Announcements.Announcements.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface NewsRepository extends JpaRepository<News, Integer> {
    List<News> findAllByUserId(Integer userId);
    Optional<News> findById(Integer id);
    @Query("SELECT n FROM News n WHERE n.user.id = :userId AND n.createdAt BETWEEN :startOfDay AND :endOfDay")
    List<News> findAllByUserIdAndCreatedAtBetween(Integer userId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
