package com.Announcements.Announcements.mapper;

import com.Announcements.Announcements.dto.CreateNewsDTO;
import com.Announcements.Announcements.dto.NewsDTO;
import com.Announcements.Announcements.dto.UpdateNewsDTO;
import com.Announcements.Announcements.model.News;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NewsMapper {
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "gmail", source = "user.gmail")
    NewsDTO toNewsDTO(News news);
    List<NewsDTO> toNewsDTOList(List<News> news);
}
