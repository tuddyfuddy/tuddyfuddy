package com.heejuk.tuddyfuddy.authservice.exception;

public class FeignServerException extends FeignException {

    public FeignServerException(String message, int status) {
        super(message, status);
    }
}