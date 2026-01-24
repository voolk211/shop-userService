package org.example.shop_userservice.exception;

public class CardLimitException extends RuntimeException {

    public CardLimitException(String message) {
        super(message);
    }

}
