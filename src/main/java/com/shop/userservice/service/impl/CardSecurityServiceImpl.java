package com.shop.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import com.shop.userservice.model.entities.Card;
import com.shop.userservice.service.CardSecurityService;
import com.shop.userservice.service.CardService;
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
