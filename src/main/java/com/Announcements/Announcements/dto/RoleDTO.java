package com.Announcements.Announcements.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность роли")
public record RoleDTO (
        @Schema(description = "Именование роли", allowableValues = {"USER", "ADMIN"})
        String role
){}
