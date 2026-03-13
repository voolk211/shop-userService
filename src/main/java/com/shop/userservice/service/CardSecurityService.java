package com.shop.userservice.service;

public interface CardSecurityService {

    boolean isOwner(Long cardId, Long userId);

}
