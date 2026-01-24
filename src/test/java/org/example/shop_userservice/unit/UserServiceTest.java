package org.example.shop_userservice.unit;

import org.example.shop_userservice.exception.ResourceNotFoundException;
import org.example.shop_userservice.model.entities.Card;
import org.example.shop_userservice.model.entities.User;
import org.example.shop_userservice.repository.CardRepository;
import org.example.shop_userservice.repository.UserRepository;
import org.example.shop_userservice.service.UserService;
import org.example.shop_userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_WhenEmailIsUnique_ShouldSaveAndReturnUser(){
        String name = "Bob";
        String surname = "Dylan";
        LocalDate birthDate = LocalDate.now();
        String email = "bob@gmail.com";
        Boolean active = true;

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        User testUser = new User(name, surname, birthDate, email, active);

        User result = userService.createUser(testUser);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getSurname()).isEqualTo(surname);
        assertThat(result.getBirthDate()).isEqualTo(birthDate);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getActive()).isEqualTo(active);

        verify(userRepository).existsByEmail(email);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WhenEmailAlreadyExists_ShouldThrowIllegalArgumentException() {
        String name = "Duplicate";
        String surname = "Dylan";
        LocalDate birthDate = LocalDate.now();
        String email = "duplicate@example.com";
        Boolean active = true;

        when(userRepository.existsByEmail(email)).thenReturn(true);

        User testUser = new User(name, surname, birthDate, email, active);

        assertThatThrownBy(() -> userService.createUser(testUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already in use");

        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        String name = "Bob";
        String surname = "Dylan";
        LocalDate birthDate = LocalDate.now();
        String email = "bob@gmail.com";
        Boolean active = true;
        User testUser = new User(name, surname, birthDate, email, active);
        testUser.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(5L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getSurname()).isEqualTo(surname);
        assertThat(result.getBirthDate()).isEqualTo(birthDate);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getActive()).isEqualTo(active);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateUser() {
        String name = "Bob";
        String surname = "Dylan";
        LocalDate birthDate = LocalDate.now();
        String email = "bob@gmail.com";
        Boolean active = true;
        Long id = 5L;

        String newName = "Bob1";
        String newSurname = "Dylan1";
        String newEmail = "bob@gmail.com1";
        LocalDate newBirthDate = LocalDate.of(1924, 3, 3);
        Boolean newActive = false;
        User testUser = new User(name, surname, birthDate, email, active);
        testUser.setId(id);

        User newUser = new User(newName, newSurname, newBirthDate ,newEmail, newActive);
        newUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(newUser);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getSurname()).isEqualTo(newSurname);
        assertThat(result.getBirthDate()).isEqualTo(newBirthDate);
        assertThat(result.getEmail()).isEqualTo(newEmail);
        assertThat(result.getActive()).isEqualTo(newActive);
    }

    @Test
    void updateUser_WhenOnlyNameProvided_ShouldUpdateOnlyName() {
        String name = "Bob";
        String surname = "Dylan";
        LocalDate birthDate = LocalDate.now();
        String email = "bob@gmail.com";
        Boolean active = true;
        Long id = 5L;

        String newName = "Bob1";

        User testUser = new User(name, surname, birthDate, email, active);
        testUser.setId(id);

        User newUser = new User();
        newUser.setId(id);
        newUser.setName(newName);

        when(userRepository.findById(id)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(newUser);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getSurname()).isEqualTo(surname);
        assertThat(result.getBirthDate()).isEqualTo(birthDate);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getActive()).isEqualTo(active);
    }
}
