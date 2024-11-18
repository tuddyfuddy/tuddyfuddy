package com.heejuk.tuddyfuddy.authservice.exception;

public abstract class FeignException extends RuntimeException {

    private final int status;

    protected FeignException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
