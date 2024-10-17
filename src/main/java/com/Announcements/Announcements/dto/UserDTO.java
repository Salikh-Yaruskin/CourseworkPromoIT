package com.Announcements.Announcements.dto;

import com.Announcements.Announcements.model.Status;

public record UserDTO(
        int id,
        String username,
        String gmail,
        String password
) {}
