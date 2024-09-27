package com.Announcements.Announcements.model;

import jakarta.persistence.*;
import org.springframework.security.core.userdetails.User;

@Entity
public class News {
    @Id
    private int id;
    String name;
    String description;
    @ManyToOne
    @JoinColumn(name = "userId")
    private Users user;
    @Column(name = "view_count", nullable = false)
    private Integer ViewCount = 0;

    public News() {
    }

    public News(String name, String description, Users user){
        this.name = name;
        this.description = description;
        this.user = user;
    }

    public int getId() {
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


    @Override
    public String toString() {
        return "News{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}
