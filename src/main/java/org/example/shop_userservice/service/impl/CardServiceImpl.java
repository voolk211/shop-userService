package org.example.shop_userservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.shop_userservice.exception.ResourceNotFoundException;
import org.example.shop_userservice.model.entities.Card;
import org.example.shop_userservice.model.entities.User;
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
        return cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    }

    @Transactional
    @Override
    public Card updateCard(Card card) {
        Card currentCard = cardRepository.findById(card.getId()).orElseThrow(() -> new ResourceNotFoundException("Card not found"));
        if (card.getUser()!=null){
            currentCard.setUser(card.getUser());
        }
        if (card.getHolder()!=null){
            currentCard.setHolder(card.getHolder());
        }
        if (card.getExpirationDate()!=null){
            currentCard.setExpirationDate(card.getExpirationDate());
        }
        if (card.getActive()!=null){
            currentCard.setActive(card.getActive());
        }
        return cardRepository.save(currentCard);
    }

    @Transactional
    @Override
    public Card patchCard(Long id, Boolean active) {
        if (!cardRepository.existsById(id)) {
            throw new ResourceNotFoundException("Card not found");
        }
        cardRepository.setActiveById(id, active);
        return cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    }

    @Transactional
    @Override
    public void deleteCard(Long cardId){
        cardRepository.deleteById(cardId);
    }

}
