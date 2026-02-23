package org.example.shopuserservice.unit;

import org.assertj.core.api.AssertionsForClassTypes;
import org.example.shopuserservice.exception.ResourceNotFoundException;
import org.example.shopuserservice.model.entities.Card;
import org.example.shopuserservice.model.entities.User;
import org.example.shopuserservice.repository.CardRepository;
import org.example.shopuserservice.repository.UserRepository;
import org.example.shopuserservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

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
        assertThat(testUser.getName()).isEqualTo(name);
        assertThat(testUser.getSurname()).isEqualTo(surname);
        assertThat(testUser.getBirthDate()).isEqualTo(birthDate);
        assertThat(testUser.getEmail()).isEqualTo(email);
        assertThat(testUser.getActive()).isEqualTo(active);

        User sameUser = new User(name, surname, birthDate, email, active);
        User differentUser = new User("Alice", "Smith", birthDate, "alice@test.com", false);

        assertThat(testUser).isEqualTo(sameUser);
        assertThat(testUser.hashCode()).isEqualTo(sameUser.hashCode());

        assertThat(testUser).isNotEqualTo(differentUser);
        assertThat(testUser).isNotEqualTo(null);
        assertThat(testUser).isNotEqualTo(new Object());

        Set<User> userSet = new HashSet<>();
        userSet.add(testUser);
        userSet.add(sameUser);
        assertThat(userSet).hasSize(1);

        userSet.add(differentUser);
        assertThat(userSet).hasSize(2);

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
    void updateUser_WhenEmailAlreadyExists_ShouldThrowIllegalArgumentException() {
        String name = "Duplicate";
        String surname = "Dylan";
        LocalDate birthDate = LocalDate.now();
        String email = "duplicate@example.com";
        String newEmail = "newduplicate@example.com";
        Boolean active = true;
        Long id = 5L;

        User testUser = new User(name, surname, birthDate, email, active);
        testUser.setId(id);

        when(userRepository.existsByEmail(newEmail)).thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.of(testUser));

        User newUser = new User(name, surname, birthDate, newEmail, active);
        newUser.setId(id);

        assertThatThrownBy(() -> userService.updateUser(newUser))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Email already in use");

        verify(userRepository).existsByEmail(newEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ResourceNotFoundException() {

        String name = "Bob";
        String surname = "Dylan";
        LocalDate birthDate = LocalDate.now();
        String email = "bob@gmail.com";
        Boolean active = true;
        Long id = 999L;

        User testUser = new User(name, surname, birthDate, email, active);
        testUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void patchUser_WhenUserDoesNotExist_ResourceNotFoundException() {
        Long id = 999L;

        when(userRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> userService.patchUser(id, false))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).existsById(id);
        verify(userRepository, never()).setActiveById(anyLong(), anyBoolean());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void patchUser_WhenUserExists_ShouldPatchUser() {
        String name = "Bob";
        String surname = "Dylan";
        LocalDate birthDate = LocalDate.now();
        String email = "bob@gmail.com";
        Boolean active = true;
        Boolean newActiveValue = false;
        Long id = 5L;

        User testUser = new User(name, surname, birthDate, email, active);
        testUser.setId(id);

        User newUser = new User(name, surname, birthDate, email, newActiveValue);
        newUser.setId(id);

        when(userRepository.existsById(id)).thenReturn(true);
        doNothing().when(userRepository).setActiveById(id, newActiveValue);
        when(userRepository.findById(id)).thenReturn(Optional.of(newUser));

        User result = userService.patchUser(id, newActiveValue);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getSurname()).isEqualTo(surname);
        assertThat(result.getBirthDate()).isEqualTo(birthDate);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getActive()).isEqualTo(newActiveValue);

        verify(userRepository).existsById(id);
        verify(userRepository).setActiveById(id, newActiveValue);
        verify(userRepository).findById(id);
    }


    @Test
    void getCardsByUserId_ShouldReturnCards(){
        String name = "Bob";
        String surname = "Dylan";
        LocalDate birthDate = LocalDate.now();
        String email = "bob@gmail.com";
        Boolean active = true;
        Long id = 5L;
        List<Card> cards = new ArrayList<>();

        User testUser = new User(name, surname, birthDate, email, active);
        testUser.setId(id);
        testUser.setCards(cards);

        when(userRepository.findById(id)).thenReturn(Optional.of(testUser));

        List<Card> newCards =  userService.getCardsByUserId(id);

        assertThat(newCards).isEqualTo(cards);
        verify(userRepository).findById(id);
    }

    @Test
    void getCardsByUserId_WhenUserDoesNotExist_ResourceNotFoundException(){
        Long id = 999L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCardsByUserId(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(id);
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        Long id = 5L;
        when(userRepository.existsById(id)).thenReturn(true);
        userService.deleteUser(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteUser_WhenCardDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        AssertionsForClassTypes.assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getAllUsersByNameAndSurname_ShouldReturnFilteredUsers(){
        String name = "Bob";
        String surname = "Dylan";
        LocalDate birthDate = LocalDate.now();
        String email = "bob@gmail.com";
        Boolean active = true;
        User testUser = new User(name, surname, birthDate, email, active);

        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(testUser);
        Page<User> page = new PageImpl<>(users, Pageable.unpaged(), users.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<User> result = userService.getAllUsersByNameAndSurname(pageable, name, surname);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo(name);
    }

    @Test
    void getAllUsersByNameAndSurname_WhenThereAreNoUsers_ShouldReturnEmptyPage(){
        String name = "Bob";
        String surname = "Dylan";

        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = new ArrayList<>();
        Page<User> page = new PageImpl<>(users, Pageable.unpaged(), users.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<User> result = userService.getAllUsersByNameAndSurname(pageable, name, surname);

        assertThat(result.getContent()).hasSize(0);

        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllCardsByUserNameAndSurname_ShouldReturnFilteredCards(){
        String name = "Bob";
        String surname = "Dylan";

        Card card = new Card();

        Pageable pageable = PageRequest.of(0, 10);
        List<Card> cards = List.of(card);
        Page<Card> page = new PageImpl<>(cards, Pageable.unpaged(), cards.size());

        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Card> result = userService.getAllCardsByUserNameAndSurname(pageable, name, surname);

        assertThat(result.getContent()).hasSize(1);
        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllCardsByUserNameAndSurname_WhenThereAreNoCards_ShouldReturnEmptyPage(){
        String name = "Bob";
        String surname = "Dylan";

        Pageable pageable = PageRequest.of(0, 10);
        List<Card> cards = new ArrayList<>();
        Page<Card> page = new PageImpl<>(cards, Pageable.unpaged(), cards.size());

        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Card> result = userService.getAllCardsByUserNameAndSurname(pageable, name, surname);

        assertThat(result.getContent()).hasSize(0);
        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
    }

}
