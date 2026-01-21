package org.example.shop_userservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.shop_userservice.model.Card;
import org.example.shop_userservice.model.User;
import org.example.shop_userservice.repository.UserRepository;
import org.example.shop_userservice.service.UserService;
import org.example.shop_userservice.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.UpdateSpecification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    @Override
    public User createUser(User user) {
        validateCardLimit(user.getCards());
        userRepository.save(user);
        return user;
    }



    private void validateCardLimit(List<Card> cards) {
        if (cards != null && cards.size() > 5) {
            throw new IllegalArgumentException("A user cannot have more than 5 cards.");
        }
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable, String name, String surname) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));
        return userRepository.findAll(spec, pageable);
    }

    @Override
    public Page<Card> getAllCardsByNameAndSurname(Pageable pageable, String name, String surname) {
        return null;
    }

    @Override
    public User updateUser(User user) {
        User currentUser = getUserById(user.getId());
        currentUser.setName(user.getName());
        currentUser.setSurname(user.getSurname());
        currentUser.setBirthDate(user.getBirthDate());
        currentUser.setEmail(user.getEmail());
        currentUser.setActive(user.getActive());
        return userRepository.save(currentUser);
    }

    @Override
    public User activateUser(Long id) {
        userRepository.setActiveById(id, true);
        return userRepository.getUserById(id);
    }

    @Override
    public User deactivateUser(Long id) {
        userRepository.setActiveById(id, false);
        return userRepository.getUserById(id);
    }

    @Override
    public List<Card> getCardsByUserId(Long userId) {
        User user = userRepository.getUserById(userId);
        return user.getCards();
    }
}
