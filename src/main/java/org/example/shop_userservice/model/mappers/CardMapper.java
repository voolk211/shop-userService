package org.example.shop_userservice.model.mappers;

import org.example.shop_userservice.model.dto.CardDto;
import org.example.shop_userservice.model.entities.Card;
import org.example.shop_userservice.model.entities.User;
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

