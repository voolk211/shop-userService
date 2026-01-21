package org.example.shop_userservice.model.mappers;

import org.example.shop_userservice.model.dto.CardDto;
import org.example.shop_userservice.model.entities.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "user", ignore = true)
    Card toEntity(CardDto cardDto);

    @Mapping(target = "userId", source = "user.id")
    CardDto toDto(Card card);

}
