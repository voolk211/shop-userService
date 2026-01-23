package org.example.shop_userservice.model.mappers;

import org.example.shop_userservice.model.dto.UserDto;
import org.example.shop_userservice.model.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserDto userDto);

    UserDto toDto(User user);

    Page<UserDto> toDto(Page<User> users) ;
}
