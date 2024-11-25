package com.Announcements.Announcements.dto;

public record UserCreateDTO(
        int id,
        String username,
        String gmail,
        String password
) { }
