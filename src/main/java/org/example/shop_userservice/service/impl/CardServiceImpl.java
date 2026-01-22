package org.example.shop_userservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.shop_userservice.model.entities.Card;
import org.example.shop_userservice.repository.CardRepository;
import org.example.shop_userservice.service.CardService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CacheManager cacheManager;

    @Transactional
    @Override
    @CacheEvict(value = "UserService::getCardsByUserId", key = "#card.user.id")
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
    @Cacheable(value = "CardService::getCardById", key = "#id")
    public Card getCardById(Long id) {
        return cardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Card not found"));
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(value = "CardService::getCardById", key = "#card.id"),
            @CacheEvict(value = "UserService::getCardsByUserId", key = "#card.user.id")
    })
    public Card updateCard(Card card) {
        Card currentCard = cardRepository.findById(card.getId()).orElseThrow(() -> new EntityNotFoundException("Card not found"));
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
        Card card = cardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Card not found")); // store it once
        evictCardAndUserCardCache(card.getUser().getId(), id);
        return card;
    }

    @Transactional
    @Override
    public Card deactivateCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new EntityNotFoundException("Card not found");
        }
        cardRepository.setActiveById(id, false);
        Card card = cardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Card not found")); // store it once
        evictCardAndUserCardCache(card.getUser().getId(), id);
        return card;
    }

    @Transactional
    @Override
    public void deleteCard(Long id){
        Card card = cardRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Card not found")); // store it once
        cardRepository.deleteById(id);
        evictCardAndUserCardCache(card.getUser().getId(), id);
    }

    private void evictCardAndUserCardCache(Long userId, Long cardId){
        Cache userChache = cacheManager.getCache("UserService::getCardsByUserId");
        Cache cardChache = cacheManager.getCache("CardService::getCardById");

        if (userChache != null) {
            userChache.evictIfPresent(userId);
        }
        if (cardChache != null) {
            cardChache.evictIfPresent(cardId);
        }
    }
}
