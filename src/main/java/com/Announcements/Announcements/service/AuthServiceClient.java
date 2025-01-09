package com.Announcements.Announcements.service;

import com.Announcements.Announcements.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "auth-service", url = "http://localhost:9091")
public interface AuthServiceClient {

    @GetMapping("/api/v1/getallusers")
    List<UserDTO> getAllUsers();
}
