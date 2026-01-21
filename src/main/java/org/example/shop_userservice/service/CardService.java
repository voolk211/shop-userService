package org.example.shop_userservice.service;

import org.example.shop_userservice.model.entities.Card;
import org.springframework.transaction.annotation.Transactional;

public interface CardService {

    Card createCard(Card card);

    Card getCardById(Long id);

    Card updateCard(Card card);

    Card activateCard(Long id);

    Card deactivateCard(Long id);

    void deleteCard(Long cardId);
}
