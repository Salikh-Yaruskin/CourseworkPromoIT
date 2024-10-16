package com.Announcements.Announcements.model;

import com.Announcements.Announcements.dto.CreateNewsDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class News {
    @Id
    private int id;
    String name;
    String description;
    @ManyToOne
    private Users user;
    @Column(name = "view_count", nullable = false)
    private Integer ViewCount;
    private Status status;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public News() {
    }

    public News(CreateNewsDTO newsDTO) {
        this.id = newsDTO.id();
        this.name = newsDTO.name();
        this.description = newsDTO.description();
        this.status = newsDTO.status();
        this.ViewCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public Integer getViewCount() {
        return ViewCount;
    }

    public void setViewCount(Integer viewCount) {
        ViewCount = viewCount;
    }

    public Status getStatus(){
        return status;
    }

    public void setStatus(Status status){
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "News{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", user='" + user + '\'' +
                ", status='" + status + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
