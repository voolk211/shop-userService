package com.shop.userservice.unit;

import com.shop.userservice.model.entities.User;
import com.shop.userservice.repository.UserRepository;
import com.shop.userservice.service.impl.UserPersistenceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserPersistenceServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserPersistenceServiceImpl userPersistenceService;

    @Test
    void saveUser_ShouldCallSaveAndFlushAndReturnSavedUser() {
        User user = new User();
        user.setEmail("test@test.com");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@test.com");

        when(userRepository.saveAndFlush(user)).thenReturn(savedUser);

        User result = userPersistenceService.saveUser(user);

        assertThat(result).isEqualTo(savedUser);
        verify(userRepository).saveAndFlush(user);
    }

}
