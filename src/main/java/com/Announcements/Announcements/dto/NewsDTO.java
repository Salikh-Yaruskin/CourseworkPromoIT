package com.Announcements.Announcements.dto;

import com.Announcements.Announcements.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Сущность новости")
public record NewsDTO(
        @Schema(description = "Уникальный id новости", example = "4", accessMode = Schema.AccessMode.READ_ONLY)
        int id,
        @Schema(description = "Название новости", example = "Сегодная в Ульяновске выстовка техники")
        String name,
        @Schema(description = "Id пользователя")
        Integer user,
        @Schema(description = "Описание новости", example = "Любое описание новости")
        String description,
        @Schema(description = "Количество просмотров у новости", example = "10")
        Integer viewCount,
        @Schema(description = "Статус новости")
        Status status
){}