package com.Announcements.Announcements.dto;

import com.Announcements.Announcements.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность Пользователя")
public record UserDTO(
        @Schema(description = "Уникальный id пользователя", example = "15")
        int id,
        @Schema(description = "Имя пользователя", example = "ivan")
        String username,
        @Schema(description = "Почта пользователя", example = "ivan@gmail.com")
        String gmail,
        @Schema(description = "Роль пользователя", allowableValues = {"USER", "ADMIN"})
        String role
) {}
