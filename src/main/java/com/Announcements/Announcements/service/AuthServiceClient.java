package com.Announcements.Announcements.service;

import com.Announcements.Announcements.dto.UserCreateDTO;
import com.Announcements.Announcements.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "auth-service", url = "http://localhost:9091")
public interface AuthServiceClient {

    @GetMapping("api/v1/users")
    List<UserDTO> getAllUsers();

    @PostMapping("api/v1/getuserbyname")
    Optional<UserDTO> getUserByName(String name);

    @PostMapping("api/v1/getbyid")
    UserDTO getUserById(Integer id);

    @PostMapping("api/v1/register")
    UserCreateDTO register(UserCreateDTO userCreateDTO);

    @PutMapping("api/v1/block")
    UserDTO blockUser(@RequestParam Integer id, @RequestParam String username);

    @PutMapping("api/v1/unblock")
    UserDTO unblock(@RequestParam Integer id, @RequestParam String username);

    @PutMapping("api/v1/limits")
    UserDTO updateLimits(@RequestParam Integer id, @RequestBody Integer newLimit);

    @PutMapping("api/v1/updaterole")
    UserDTO updateRole(@RequestParam Integer id, @RequestBody String role);

    @PostMapping("/api/v1/check")
    ResponseEntity<String> checkUser(@RequestParam Integer id);
}
