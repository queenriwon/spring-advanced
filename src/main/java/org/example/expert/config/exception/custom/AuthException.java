package org.example.expert.config.exception.custom;

public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}
