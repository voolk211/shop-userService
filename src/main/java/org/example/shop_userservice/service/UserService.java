package org.example.shop_userservice.service;

import org.example.shop_userservice.model.Card;
import org.example.shop_userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface UserService {

    User createUser(User user);

    User getUserById(Long id);

    Page<User> getAllUsers(Pageable pageable, String name, String surname);

    Page<Card> getAllCardsByNameAndSurname(Pageable pageable, String name, String surname);

    User updateUser(User user);

    User activateUser(Long id);

    User deactivateUser(Long id);

    List<Card> getCardsByUserId(Long userId);
}
