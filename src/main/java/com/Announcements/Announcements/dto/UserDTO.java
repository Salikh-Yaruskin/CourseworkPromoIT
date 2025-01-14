package com.Announcements.Announcements.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность Пользователя")
public record UserDTO(
        @Schema(description = "Уникальный id пользователя", example = "15")
        int id,
        @Schema(description = "Имя пользователя", example = "ivan")
        String username,
        @Schema(description = "Почта пользователя", example = "ivan@gmail.com")
        String gmail,
        @Schema(description = "Пароль")
        String password,
        @Schema(description = "Роль пользователя", allowableValues = {"USER", "ADMIN"})
        String role,
        @Schema(description = "Лимит по новостям", example = "5")
        Integer limitNews
) {}
