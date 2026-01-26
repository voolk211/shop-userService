package org.example.shop_userservice.unit;

import jakarta.persistence.EntityNotFoundException;
import org.example.shop_userservice.exception.CardLimitException;
import org.example.shop_userservice.exception.ResourceNotFoundException;
import org.example.shop_userservice.model.entities.Card;
import org.example.shop_userservice.model.entities.User;
import org.example.shop_userservice.repository.CardRepository;
import org.example.shop_userservice.repository.UserRepository;
import org.example.shop_userservice.service.CardService;
import org.example.shop_userservice.service.impl.CardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    CacheManager cacheManager;

    @Mock
    CardRepository cardRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    CardServiceImpl cardService;

    @Test
    void createCard_WhenCardIsValidAndLimitNotExceeded_ShouldSaveAndReturnCard() {
        Long userId = 1L;
        String number = "1234567890123456";
        String holder = "Bob";
        LocalDate expirationDate = LocalDate.now();
        Boolean active = true;

        User user = new User();
        user.setId(userId);

        Card card = new Card();
        card.setNumber(number);
        card.setHolder(holder);
        card.setExpirationDate(expirationDate);
        card.setActive(active);
        card.setUser(user);

        when(cardRepository.countByUserId(userId)).thenReturn(3L);
        when(userRepository.existsById(user.getId())).thenReturn(true);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        Card result = cardService.createCard(card);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNumber()).isEqualTo(number);
        assertThat(result.getHolder()).isEqualTo(holder);
        assertThat(result.getExpirationDate()).isEqualTo(expirationDate);
        assertThat(result.getActive()).isEqualTo(active);
        assertThat(result.getUser().getId()).isEqualTo(userId);

        verify(cardRepository).countByUserId(userId);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_WhenCardIdAlreadyExists_ShouldThrowCardLimitException() {
        Long cardId = 100L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Card existingCard = new Card();
        existingCard.setId(cardId);
        existingCard.setUser(user);

        when(userRepository.existsById(user.getId())).thenReturn(true);
        when(cardRepository.existsById(cardId)).thenReturn(true);

        assertThatThrownBy(() -> cardService.createCard(existingCard))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Card already exists.");

        verify(cardRepository).existsById(cardId);
        verify(cardRepository, never()).countByUserId(any());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void createCard_WhenUserNotExists_ShouldThrowCardLimitException() {
        Long cardId = 100L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Card existingCard = new Card();
        existingCard.setId(cardId);
        existingCard.setUser(user);

        when(userRepository.existsById(user.getId())).thenReturn(false);

        assertThatThrownBy(() -> cardService.createCard(existingCard))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User not exists");

        verify(userRepository).existsById(user.getId());
        verify(cardRepository, never()).countByUserId(any());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void createCard_WhenUserHasFiveCards_ShouldThrowIllegalArgumentException() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        Card newCard = new Card();
        newCard.setUser(user);

        when(userRepository.existsById(user.getId())).thenReturn(true);
        when(cardRepository.countByUserId(userId)).thenReturn(5L);

        assertThatThrownBy(() -> cardService.createCard(newCard))
                .isInstanceOf(CardLimitException.class)
                .hasMessage("A user cannot have more than 5 cards.");

        verify(cardRepository).countByUserId(userId);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void getCardById_WhenCardExists_ShouldReturnCard() {
        Long id = 1L;
        String number = "1111222233334444";
        String holder = "Bob";
        LocalDate expirationDate = LocalDate.now();
        Boolean active = true;
        Long userId = 2L;

        User user = new User();
        user.setId(userId);

        Card card = new Card();
        card.setId(id);
        card.setNumber(number);
        card.setHolder(holder);
        card.setExpirationDate(expirationDate);
        card.setActive(active);
        card.setUser(user);

        when(cardRepository.findById(id)).thenReturn(Optional.of(card));

        Card result = cardService.getCardById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNumber()).isEqualTo(number);
        assertThat(result.getHolder()).isEqualTo(holder);
        assertThat(result.getExpirationDate()).isEqualTo(expirationDate);
        assertThat(result.getActive()).isEqualTo(active);
        assertThat(result.getUser().getId()).isEqualTo(userId);
    }

    @Test
    void getCardById_WhenCardDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Card not found");
    }

    @Test
    void updateCard_WhenCardExists_ShouldUpdateFieldsAndReturnCard() {
        Long id = 5L;
        Long oldUserId = 1L;
        Long newUserId = 2L;
        String oldNumber = "0000111122223333";
        String newHolder = "New Holder";
        LocalDate newExpirationDate = LocalDate.now();
        Boolean newActive = false;

        User oldUser = new User();
        oldUser.setId(oldUserId);

        Card currentCard = new Card();
        currentCard.setId(id);
        currentCard.setNumber(oldNumber);
        currentCard.setHolder("Old Holder");
        currentCard.setExpirationDate(LocalDate.of(2025, 1, 1));
        currentCard.setActive(true);
        currentCard.setUser(oldUser);

        User newUser = new User();
        newUser.setId(newUserId);

        Card updateRequest = new Card();
        updateRequest.setId(id);
        updateRequest.setUser(newUser);
        updateRequest.setHolder(newHolder);
        updateRequest.setExpirationDate(newExpirationDate);
        updateRequest.setActive(newActive);

        when(cardRepository.findById(id)).thenReturn(Optional.of(currentCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Card result = cardService.updateCard(updateRequest);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getNumber()).isEqualTo(oldNumber);
        assertThat(result.getHolder()).isEqualTo(newHolder);
        assertThat(result.getExpirationDate()).isEqualTo(newExpirationDate);
        assertThat(result.getActive()).isEqualTo(newActive);
        assertThat(result.getUser().getId()).isEqualTo(newUserId);

        verify(cardRepository).findById(id);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void updateCard_WhenCardDoesNotExist_ShouldThrowResourceNotFoundException() {
        Card nonExistentCard = new Card();
        nonExistentCard.setId(999L);

        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.updateCard(nonExistentCard))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Card not found");

        verify(cardRepository).findById(999L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void patchCard_WhenCardExists_ShouldUpdateActiveAndReturnCard() {
        Long id = 10L;
        Long userId = 3L;
        Boolean newActive = false;

        User user = new User();
        user.setId(userId);

        Card cardAfterUpdate = new Card();
        cardAfterUpdate.setId(id);
        cardAfterUpdate.setActive(newActive);
        cardAfterUpdate.setUser(user);

        when(cardRepository.existsById(id)).thenReturn(true);
        doNothing().when(cardRepository).setActiveById(id, newActive);
        when(cardRepository.findById(id)).thenReturn(Optional.of(cardAfterUpdate));

        Cache userCache = mock(Cache.class);
        Cache cardCache = mock(Cache.class);
        when(cacheManager.getCache("UserService::getCardsByUserId")).thenReturn(userCache);
        when(cacheManager.getCache("CardService::getCardById")).thenReturn(cardCache);

        Card result = cardService.patchCard(id, newActive);

        assertThat(result.getActive()).isEqualTo(newActive);
        assertThat(result.getUser().getId()).isEqualTo(userId);

        verify(cardRepository).existsById(id);
        verify(cardRepository).setActiveById(id, newActive);
        verify(cardRepository, times(2)).findById(id);
        verify(userCache).evictIfPresent(userId);
        verify(cardCache).evictIfPresent(id);
    }

    @Test
    void patchCard_WhenCardDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(cardRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> cardService.patchCard(999L, true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Card not found");

        verify(cardRepository).existsById(999L);
        verify(cardRepository, never()).setActiveById(anyLong(), anyBoolean());
        verify(cardRepository, never()).findById(anyLong());
    }

    @Test
    void deleteCard_WhenCardExists_ShouldDeleteAndEvictCache() {
        Long id = 7L;
        Long userId = 4L;

        User user = new User();
        user.setId(userId);

        Card cardToDelete = new Card();
        cardToDelete.setId(id);
        cardToDelete.setUser(user);

        when(cardRepository.findById(id)).thenReturn(Optional.of(cardToDelete));
        doNothing().when(cardRepository).deleteById(id);

        Cache userCache = mock(Cache.class);
        Cache cardCache = mock(Cache.class);
        when(cacheManager.getCache("UserService::getCardsByUserId")).thenReturn(userCache);
        when(cacheManager.getCache("CardService::getCardById")).thenReturn(cardCache);

        cardService.deleteCard(id);

        verify(cardRepository).findById(id);
        verify(cardRepository).deleteById(id);
        verify(userCache).evictIfPresent(userId);
        verify(cardCache).evictIfPresent(id);
    }

    @Test
    void deleteCard_WhenCardDoesNotExist_ShouldThrowEntityNotFoundException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.deleteCard(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Card not found");

        verify(cardRepository).findById(999L);
        verify(cardRepository, never()).deleteById(anyLong());
    }

}
