package com.Announcements.Announcements.dto;

import com.Announcements.Announcements.model.Roles;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сущность роли")
public record RoleDTO (
        @Schema(description = "Именование роли")
        Roles role
){}
