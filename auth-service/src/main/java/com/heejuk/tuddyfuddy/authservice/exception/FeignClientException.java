package com.heejuk.tuddyfuddy.authservice.exception;

public class FeignClientException extends RuntimeException {
    private final int status;

    public FeignClientException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
