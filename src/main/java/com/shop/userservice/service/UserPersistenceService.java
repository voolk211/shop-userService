package com.shop.userservice.service;

import com.shop.userservice.model.entities.User;

public interface UserPersistenceService {

    User saveUser(User user);

}
