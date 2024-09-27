package com.Announcements.Announcements.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_view")
public class UserView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false) // Убедитесь, что id не изменяется вручную
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    public UserView() {
    }

    public UserView(Users user, News news) {
        this.user = user;
        this.news = news;
    }

    public Integer getId() {
        return id;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }
}
