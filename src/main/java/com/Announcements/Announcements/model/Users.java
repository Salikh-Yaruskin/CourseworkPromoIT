package com.Announcements.Announcements.model;

import com.Announcements.Announcements.dto.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@Schema(description = "Основная сущносить пользователя")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный id пользователя", example = "15")
    private int id;
    @Schema(description = "Имя пользователя", example = "ivan")
    @Column
    private String username;
    @Schema(description = "Почта пользователя", example = "ivan@gmail.com")
    @Column
    private String gmail;
    @Schema(description = "Пароль", example = "ivan")
    @Column
    private String password;
    @Schema(description = "Роль пользователя", allowableValues = {"USER", "ADMIN"})
    @Column
    private String role;
    @Schema(description = "Статус пользователя")
    @Column
    private Status status;
    @Schema(description = "Количество доступных публикаций в день", example = "5")
    @Column(name = "limit_news")
    private Integer limitNews = 5;

    public Users(UserDTO userDTO) {
        this.id = userDTO.id();
        this.username = userDTO.username();
        this.gmail = userDTO.gmail();
        this.limitNews = 5;
    }
}
