package com.Announcements.Announcements.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_view")
@Data
@NoArgsConstructor
public class UserView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    public UserView(Users user, News news) {
        this.user = user;
        this.news = news;
    }
}
