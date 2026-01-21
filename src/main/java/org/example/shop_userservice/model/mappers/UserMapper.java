package org.example.shop_userservice.model.mappers;

import org.example.shop_userservice.model.dto.UserDto;
import org.example.shop_userservice.model.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserDto userDto);

    UserDto toDto(User user);
}
