package org.example.shop_userservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.shop_userservice.model.entities.Card;
import org.example.shop_userservice.repository.CardRepository;
import org.example.shop_userservice.service.CardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    @Transactional
    @Override
    public Card createCard(Card card) {
        if (card.getId()!=null && cardRepository.existsById(card.getId())) {
            throw new IllegalStateException("Card already exists.");
        }
        validateCardLimit(card.getUser().getId());
        return cardRepository.save(card);
    }

    private void validateCardLimit(Long userId) {
        Long cardCount = cardRepository.countByUserId(userId);
        if (cardCount != null && cardCount >= 5) {
            throw new IllegalArgumentException("A user cannot have more than 5 cards.");
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Card getCardById(Long id) {
        return cardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Card not found"));
    }

    @Transactional
    @Override
    public Card updateCard(Card card) {
        Card currentCard = getCardById(card.getId());
        currentCard.setUser(card.getUser());
        currentCard.setHolder(card.getHolder());
        currentCard.setExpirationDate(card.getExpirationDate());
        currentCard.setActive(card.isActive());
        return cardRepository.save(currentCard);
    }

    @Transactional
    @Override
    public Card activateCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new EntityNotFoundException("Card not found");
        }
        cardRepository.setActiveById(id, true);
        return getCardById(id);
    }

    @Transactional
    @Override
    public Card deactivateCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new EntityNotFoundException("Card not found");
        }
        cardRepository.setActiveById(id, false);
        return getCardById(id);
    }

    @Transactional
    @Override
    public void deleteCard(Long cardId){
        cardRepository.deleteById(cardId);
    }

}
