package com.Announcements.Announcements.model;

import com.Announcements.Announcements.dto.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "users")
@Schema(description = "Основная сущносить пользователя")
@NoArgsConstructor
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный идентификатор пользователя", example = "1")
    private int id;

    @Column(nullable = false, length = 20)
    @Size(max = 20, message = "Имя пользователя не может быть больше 20")
    @Schema(description = "Имя пользователя", example = "ivan")
    @NotBlank(message = "Имя обязательно")
    private String username;

    @Column(nullable = false)
    @Schema(description = "Почта пользователя", example = "ivan@gmail.com")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Неверный формат!"
    )
    @NotBlank(message = "Почта обязательна!")
    private String gmail;

    @Column(nullable = false)
    @Schema(description = "Пароль", example = "ivan")
    @NotBlank(message = "Пароль обязателень!")
    private String password;

    @Column(nullable = false)
    @Schema(description = "Роль пользователя")
    @Enumerated(EnumType.STRING)
    private Roles role;

    @Column(nullable = false)
    @Schema(description = "Статус пользователя")
    private Status status;

    @Column(name = "limit_news", nullable = false)
    @Schema(description = "Количество доступных публикаций в день", example = "5")
    private Integer limitNews;

    public Users(UserDTO userDTO) {
        this.id = userDTO.id();
        this.username = userDTO.username();
        this.gmail = userDTO.gmail();
    }
}
