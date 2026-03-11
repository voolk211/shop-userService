package com.shop.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import com.shop.userservice.exception.CardLimitException;
import com.shop.userservice.exception.ResourceNotFoundException;
import com.shop.userservice.model.entities.Card;
import com.shop.userservice.repository.CardRepository;
import com.shop.userservice.repository.UserRepository;
import com.shop.userservice.service.CardService;
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
    private final UserRepository userRepository;
    private final CacheManager cacheManager;

    @Transactional
    @Override
    public Card createCard(Card card) {
        if (!userRepository.existsById(card.getUser().getId())){
            throw new ResourceNotFoundException("User not exists.");
        }
        if (card.getId()!=null && cardRepository.existsById(card.getId())) {
            throw new IllegalStateException("Card already exists.");
        }
        validateCardLimit(card.getUser().getId());
        return cardRepository.save(card);
    }

    private void validateCardLimit(Long userId) {
        Long cardCount = cardRepository.countByUserId(userId);
        if (cardCount != null && cardCount >= 5) {
            throw new CardLimitException("A user cannot have more than 5 cards.");
        }
    }

    @Transactional(readOnly = true)
    @Override
    @Cacheable(value = "CardService::getCardById", key = "#id")
    public Card getCardById(Long id) {
        return getCardOrThrow(id);
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(value = "CardService::getCardById", key = "#card.id"),
            @CacheEvict(value = "UserService::getCardsByUserId", key = "#card.user.id")
    })
    public Card updateCard(Card card) {
        Card currentCard = getCardOrThrow(card.getId());
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
        Card card = getCardOrThrow(id);
        evictCardAndUserCardCache(card.getUser().getId(), id);
        return card;
    }

    @Transactional
    @Override
    public void deleteCard(Long id){
        Card card = getCardOrThrow(id);
        cardRepository.deleteById(id);
        evictCardAndUserCardCache(card.getUser().getId(), id);
    }

    private Card getCardOrThrow(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    }

    private void evictCardAndUserCardCache(Long userId, Long cardId){
        Cache userCache = cacheManager.getCache("UserService::getCardsByUserId");
        Cache cardCache = cacheManager.getCache("CardService::getCardById");

        if (userCache != null) {
            userCache.evictIfPresent(userId);
        }
        if (cardCache != null) {
            cardCache.evictIfPresent(cardId);
        }
    }
}
