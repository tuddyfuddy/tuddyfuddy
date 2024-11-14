package com.heejuk.tuddyfuddy.authservice.exception;

public class FeignClientException extends FeignException {

    public FeignClientException(String message, int status) {
        super(message, status);
    }
}
