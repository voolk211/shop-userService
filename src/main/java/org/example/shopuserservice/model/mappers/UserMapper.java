package org.example.shopuserservice.model.mappers;

import org.example.shopuserservice.model.dto.UserDto;
import org.example.shopuserservice.model.entities.User;
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
