package com.Announcements.Announcements.model;

import com.Announcements.Announcements.dto.LoginDTO;
import com.Announcements.Announcements.dto.UserCreateDTO;
import com.Announcements.Announcements.dto.UserDTO;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Users {
    @Id
    private int id;
    private String username;
    private String gmail;
    private String password;
    private String role;
    private Status status;
    private Integer limitNews = 5;

    public Users(){

    }

    public Users(UserDTO userDTO) {
        this.id = userDTO.id();
        this.username = userDTO.username();
        this.gmail = userDTO.gmail();
        this.limitNews = 5;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGmail() { return gmail;}

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getLimitNews() {
        return limitNews;
    }

    public void setLimitNews(Integer limitNews) {
        this.limitNews = limitNews;
    }

    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", gmail='" + gmail + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", limitNews='" + limitNews + '\'' +
                '}';
    }
}
