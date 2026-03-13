package com.shop.userservice.model.mappers;

import com.shop.userservice.model.dto.UserDto;
import com.shop.userservice.model.entities.User;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserDto userDto);

    UserDto toDto(User user);

    default Page<UserDto> toDto(Page<User> users){
        return users.map(this::toDto);
    }
}
