package org.example.shopuserservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.shopuserservice.model.entities.Card;
import org.example.shopuserservice.service.CardSecurityService;
import org.example.shopuserservice.service.CardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardSecurityServiceImpl implements CardSecurityService {

    private final CardService cardService;

    @Transactional
    @Override
    public boolean isOwner(Long cardId, Long userId) {
        Card card = cardService.getCardById(cardId);
        return card.getUser().getId().equals(userId);
    }

}
