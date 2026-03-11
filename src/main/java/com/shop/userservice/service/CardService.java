package com.shop.userservice.service;

import com.shop.userservice.model.entities.Card;

public interface CardService {

    Card createCard(Card card);

    Card getCardById(Long id);

    Card updateCard(Card card);

    Card patchCard(Long id, Boolean active);

    void deleteCard(Long cardId);

}
