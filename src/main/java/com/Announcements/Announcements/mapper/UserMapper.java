package com.Announcements.Announcements.mapper;

import com.Announcements.Announcements.dto.UserCreateDTO;
import com.Announcements.Announcements.dto.UserDTO;
import com.Announcements.Announcements.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserDTO toUserDTO(Users users);
    Users fromUserCreateDto(UserCreateDTO userCreateDTO);
    Users fromUserDto(UserDTO userDTO);

    List<UserDTO> toUserDTOList(List<Users> users);
}
