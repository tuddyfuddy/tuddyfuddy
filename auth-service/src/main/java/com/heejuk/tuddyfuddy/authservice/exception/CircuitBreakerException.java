package com.heejuk.tuddyfuddy.authservice.exception;

public class CircuitBreakerException extends RuntimeException {

    public CircuitBreakerException(String message) {
        super(message);
    }
}
