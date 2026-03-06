package org.example.shopuserservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.shopuserservice.model.entities.User;
import org.example.shopuserservice.repository.UserRepository;
import org.example.shopuserservice.service.UserPersistenceService;
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
