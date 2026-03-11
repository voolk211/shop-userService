package com.shop.userservice.model.mappers;

import com.shop.userservice.model.dto.CardDto;
import com.shop.userservice.model.entities.Card;
import com.shop.userservice.model.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {

    default Card toEntity(CardDto cardDto){
        Card card = new Card();
        card.setId(cardDto.getId());
        card.setNumber(cardDto.getNumber());
        card.setHolder(cardDto.getHolder());
        card.setExpirationDate(cardDto.getExpirationDate());
        card.setActive(cardDto.getActive());

        User user = new User();
        user.setId(cardDto.getUserId());
        card.setUser(user);
        return card;
    }

    @Mapping(target = "userId", source = "user.id")
    CardDto toDto(Card card);

    @Mapping(target = "userId", source = "user.id")
    List<CardDto> toDto(List<Card> cards);

    @Mapping(target = "userId", source = "user.id")
    default Page<CardDto> toDto(Page<Card> cards){
        return cards.map(this::toDto);
    }

}

