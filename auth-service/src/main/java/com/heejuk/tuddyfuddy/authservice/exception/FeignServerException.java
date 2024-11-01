package com.heejuk.tuddyfuddy.authservice.exception;

public class FeignServerException extends RuntimeException {
    private final int status;

    public FeignServerException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}