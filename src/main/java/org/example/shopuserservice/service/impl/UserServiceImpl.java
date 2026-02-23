package org.example.shopuserservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.shopuserservice.exception.ResourceNotFoundException;
import org.example.shopuserservice.model.entities.Card;
import org.example.shopuserservice.model.entities.User;
import org.example.shopuserservice.repository.CardRepository;
import org.example.shopuserservice.repository.UserRepository;
import org.example.shopuserservice.service.UserService;
import org.example.shopuserservice.specification.CardSpecification;
import org.example.shopuserservice.specification.UserSpecification;
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

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    @Override
    public User createUser(User user) {

        if (user.getId() != null && userRepository.existsById(user.getId())){
            throw new IllegalStateException("User already exists.");
        }
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())){
            throw new IllegalStateException("Email already in use");
        }
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    @Override
    @Cacheable(value = "UserService::getUserById", key = "#id")
    public User getUserById(Long id) {
        return getUserOrThrow(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<User> getAllUsersByNameAndSurname(Pageable pageable, String name, String surname) {
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
        User currentUser = getUserOrThrow(user.getId());
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail()) && !(currentUser.getEmail().equals(user.getEmail()))){
            throw new IllegalStateException("Email already in use");
        }
        if (user.getEmail()!=null){
            currentUser.setEmail(user.getEmail());
        }
        if (user.getName()!=null){
            currentUser.setName(user.getName());
        }
        if (user.getSurname()!=null){
            currentUser.setSurname(user.getSurname());
        }
        if (user.getBirthDate()!=null){
            currentUser.setBirthDate(user.getBirthDate());
        }
        if (user.getActive()!=null){
            currentUser.setActive(user.getActive());
        }
        return userRepository.save(currentUser);
    }

    @Transactional
    @Override
    @CacheEvict(value = "UserService::getUserById", key = "#id")
    public User patchUser(Long id, Boolean active) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.setActiveById(id, active);
        return getUserOrThrow(id);
    }

    @Transactional(readOnly = true)
    @Override
    @Cacheable(value = "UserService::getCardsByUserId", key = "#id")
    public List<Card> getCardsByUserId(Long id) {
        User user = getUserOrThrow(id);
        return user.getCards();
    }

    @Transactional
    @Override
    @Caching(evict = {
        @CacheEvict(value = "UserService::getUserById", key = "#id"),
        @CacheEvict(value = "UserService::getCardsByUserId", key = "#id")
    })
    public void deleteUser(Long id){
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
}
