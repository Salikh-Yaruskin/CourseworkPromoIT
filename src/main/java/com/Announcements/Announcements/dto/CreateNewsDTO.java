package com.Announcements.Announcements.dto;

import com.Announcements.Announcements.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность создания новости")
public record CreateNewsDTO(
        @Schema(description = "Уникальный id новости", example = "12", hidden = true)
        int id,
        @Schema(description = "Название новости", example = "Сегодная в Ульяновске выстовка техники")
        String name,
        @Schema(description = "Описание новости", example = "Любое описание новости")
        String description,
        @Schema(description = "Статус новости")
        Status status
) {}
