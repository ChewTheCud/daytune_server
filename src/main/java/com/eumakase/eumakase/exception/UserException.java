package com.eumakase.eumakase.exception;

public class UserException extends RuntimeException {
    public UserException(int i, String message) {
        super(message);
    }
}