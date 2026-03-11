package com.shop.userservice.service;

import com.shop.userservice.model.entities.Card;
import com.shop.userservice.model.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    User createUser(User user);

    User createUserIdempotent(User user);

    User saveUser(User user);

    User getUserById(Long id);

    Page<User> getAllUsersByNameAndSurname(Pageable pageable, String name, String surname);

    Page<Card> getAllCardsByUserNameAndSurname(Pageable pageable, String name, String surname);

    User updateUser(User user);

    User patchUser(Long id, Boolean active);

    List<Card> getCardsByUserId(Long userId);

    void deleteUser(Long userId);

}
