package com.Announcements.Announcements.model;

import com.Announcements.Announcements.dto.UserDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
public class Users {
    @Id
    private int id;
    private String username;
    private String gmail;
    private String password;
    private String role;
    private Status status;
    private Integer limitNews = 5;

    public Users(UserDTO userDTO) {
        this.id = userDTO.id();
        this.username = userDTO.username();
        this.gmail = userDTO.gmail();
        this.limitNews = 5;
    }

}
