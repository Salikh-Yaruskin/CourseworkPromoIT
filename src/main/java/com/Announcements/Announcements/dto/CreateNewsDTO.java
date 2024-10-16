package com.Announcements.Announcements.dto;

import com.Announcements.Announcements.model.Status;

public record CreateNewsDTO(
        Integer id,
        String name,
        String description,
        Status status
) {}
