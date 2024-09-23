package com.Announcements.Announcements.model;

public class News {
    String name;
    String description;

    public News(String name, String description){
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "News{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
