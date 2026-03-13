package com.shop.userservice.unit;

import com.shop.userservice.model.entities.Card;
import com.shop.userservice.model.entities.User;
import com.shop.userservice.service.CardService;
import com.shop.userservice.service.impl.CardSecurityServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardSecurityServiceTest {


    @Mock
    private CardService cardService;

    @InjectMocks
    private CardSecurityServiceImpl cardSecurityService;

    @Test
    void isOwner_WhenUserIsOwner_ShouldReturnTrue() {
        Long cardId = 1L;
        Long userId = 10L;

        User user = new User();
        user.setId(userId);

        Card card = new Card();
        card.setId(cardId);
        card.setUser(user);

        when(cardService.getCardById(cardId)).thenReturn(card);

        boolean result = cardSecurityService.isOwner(cardId, userId);

        assertThat(result).isTrue();
        verify(cardService).getCardById(cardId);
    }

    @Test
    void isOwner_WhenUserIsNotOwner_ShouldReturnFalse() {
        Long cardId = 1L;
        Long actualOwnerId = 10L;
        Long anotherUserId = 99L;

        User user = new User();
        user.setId(actualOwnerId);

        Card card = new Card();
        card.setId(cardId);
        card.setUser(user);

        when(cardService.getCardById(cardId)).thenReturn(card);

        boolean result = cardSecurityService.isOwner(cardId, anotherUserId);

        assertThat(result).isFalse();
        verify(cardService).getCardById(cardId);
    }
}


