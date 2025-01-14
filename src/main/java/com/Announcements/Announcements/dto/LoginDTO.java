package com.Announcements.Announcements.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность для вход пользователя")
public record LoginDTO(
        @Schema(description = "Имя пользователя", example = "ivan")
        String username,
        @Schema(description = "Пароль пользователя", example = "ivan")
        String password
) {}
