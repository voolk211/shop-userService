package org.example.shopuserservice.service;

import org.example.shopuserservice.model.entities.Card;

public interface CardService {

    Card createCard(Card card);

    Card getCardById(Long id);

    Card updateCard(Card card);

    Card patchCard(Long id, Boolean active);

    void deleteCard(Long cardId);
}
