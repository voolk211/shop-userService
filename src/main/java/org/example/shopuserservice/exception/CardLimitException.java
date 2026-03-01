package org.example.shopuserservice.exception;

import java.io.Serial;

public class CardLimitException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -954343048027837494L;

    public CardLimitException() {
    }

    public CardLimitException(String message) {
        super(message);
    }

    public CardLimitException(Throwable cause) {
        super(cause);
    }

    public CardLimitException(String message, Throwable cause) {
        super(message, cause);
    }

}
