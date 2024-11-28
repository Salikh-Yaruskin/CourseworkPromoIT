package com.Announcements.Announcements.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность для обновление новости")
public record UpdateNewsDTO(
        @Schema(description = "Новое название новости", example = "Сегодная в Ульяновске выстовка техники")
        String name,
        @Schema(description = "Новое описание новости", example = "Любое описание новости")
        String description
) {}
