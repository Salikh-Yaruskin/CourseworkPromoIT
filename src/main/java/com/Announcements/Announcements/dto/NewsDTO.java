package com.Announcements.Announcements.dto;

import com.Announcements.Announcements.model.Status;



public record NewsDTO(
        int id,
        String name,
        String description,
        String username,
        String gmail,
        Integer viewCount,
        Status status
){}