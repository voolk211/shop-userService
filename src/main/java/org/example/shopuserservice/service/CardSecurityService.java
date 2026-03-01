package org.example.shopuserservice.service;

public interface CardSecurityService {

    boolean isOwner(Long cardId, Long userId);

}
