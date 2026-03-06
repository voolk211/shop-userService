package org.example.shopuserservice.service;

import org.example.shopuserservice.model.entities.User;

public interface UserPersistenceService {

    User saveUser(User user);

}
