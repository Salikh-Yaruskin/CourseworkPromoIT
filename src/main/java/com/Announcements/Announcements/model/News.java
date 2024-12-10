package com.Announcements.Announcements.model;

import com.Announcements.Announcements.dto.CreateNewsDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "news")
@NoArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 150)
    @Size(max = 150, message = "Название новости превышает 150 символов!")
    @NotBlank(message = "Название обязательно!")
    private String name;

    @Column
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "view_count", nullable = false)
    private Integer ViewCount;

    @Column
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public News(CreateNewsDTO newsDTO) {
        this.id = newsDTO.id();
        this.name = newsDTO.name();
        this.description = newsDTO.description();
        this.status = newsDTO.status();
        this.ViewCount = 0;
        this.createdAt = LocalDateTime.now();
    }
}
