package org.example.shop_userservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.shop_userservice.model.entities.Card;
import org.example.shop_userservice.model.entities.User;
import org.example.shop_userservice.repository.CardRepository;
import org.example.shop_userservice.repository.UserRepository;
import org.example.shop_userservice.service.UserService;
import org.example.shop_userservice.specification.CardSpecification;
import org.example.shop_userservice.specification.UserSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public User createUser(User user) {
        if (user.getId() != null && userRepository.existsById(user.getId())){
            throw new IllegalStateException("User already exists.");
        }
        userRepository.save(user);
        return user;
    }

    @Transactional(readOnly = true)
    @Override
    @Cacheable(value = "UserService::getUserById", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<User> getAllUsers(Pageable pageable, String name, String surname) {
        Specification<User> spec = Specification
                .where(UserSpecification.hasName(name))
                .and(UserSpecification.hasSurname(surname));
        return userRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Card> getAllCardsByUserNameAndSurname(Pageable pageable, String name, String surname) {
        Specification<Card> spec = Specification
                .where(CardSpecification.cardUserHasName(name))
                .and(CardSpecification.cardUserHasSurname(surname));
        return cardRepository.findAll(spec, pageable);
    }

    @Transactional
    @Override
    @CacheEvict(value = "UserService::getUserById", key = "#user.id")
    public User updateUser(User user) {
        User currentUser = userRepository.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException("User not found"));
        currentUser.setName(user.getName());
        currentUser.setSurname(user.getSurname());
        currentUser.setBirthDate(user.getBirthDate());
        currentUser.setEmail(user.getEmail());
        currentUser.setActive(user.isActive());
        return userRepository.save(currentUser);
    }

    @Transactional
    @Override
    @CacheEvict(value = "UserService::getUserById", key = "#id")
    public User activateUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found");
        }
        userRepository.setActiveById(id, true);
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Transactional
    @Override
    @CacheEvict(value = "UserService::getUserById", key = "#id")
    public User deactivateUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found");
        }
        userRepository.setActiveById(id, false);
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    @Override
    @Cacheable(value = "UserService::getCardsByUserId", key = "#id")
    public List<Card> getCardsByUserId(Long id) {
        User user = getUserById(id);
        return user.getCards();
    }

    @Transactional
    @Override
    @Caching(evict = {
        @CacheEvict(value = "UserService::getUserById", key = "#id"),
        @CacheEvict(value = "UserService::getCardsByUserId", key = "#id")
    })
    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }
}
