package com.Announcements.Announcements.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name = "user_view")
@Entity
@NoArgsConstructor
public class UserView {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer user;

    @ManyToOne
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    public UserView(Integer user, News news) {
        this.user = user;
        this.news = news;
    }
}
