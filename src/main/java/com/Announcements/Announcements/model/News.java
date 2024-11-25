package com.Announcements.Announcements.model;

import com.Announcements.Announcements.dto.CreateNewsDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
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

    public News(CreateNewsDTO newsDTO) {
        this.id = newsDTO.id();
        this.name = newsDTO.name();
        this.description = newsDTO.description();
        this.status = newsDTO.status();
        this.ViewCount = 0;
        this.createdAt = LocalDateTime.now();
    }
}
