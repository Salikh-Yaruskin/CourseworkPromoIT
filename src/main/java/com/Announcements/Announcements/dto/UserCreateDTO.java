package com.Announcements.Announcements.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность создания пользователя")
public record UserCreateDTO(
        @Schema(description = "Уникальный id пользователя", example = "15")
        int id,
        @Schema(description = "Имя пользователя", example = "ivan")
        String username,
        @Schema(description = "Почта пользователя", example = "ivan@gmail.com")
        String gmail,
        @Schema(description = "Пароль", example = "ivan")
        String password
) { }
