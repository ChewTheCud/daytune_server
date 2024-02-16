package com.eumakase.eumakase.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    private final Integer statusCode;

    public AuthException(String message) {
        super(message);
        this.statusCode = null;
    }

    public AuthException(int status, String message) {
        super(message);
        this.statusCode = status;
    }

}