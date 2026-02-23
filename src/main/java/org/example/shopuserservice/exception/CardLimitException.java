package org.example.shopuserservice.exception;

public class CardLimitException extends RuntimeException {

    public CardLimitException(String message) {
        super(message);
    }

}
