package com.shop.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import com.shop.userservice.model.entities.User;
import com.shop.userservice.repository.UserRepository;
import com.shop.userservice.service.UserPersistenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPersistenceServiceImpl implements UserPersistenceService {

    private final UserRepository userRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public User saveUser(User user) {
        return userRepository.saveAndFlush(user);
    }
}
