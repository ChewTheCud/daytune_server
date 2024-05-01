package com.eumakase.eumakase.exception;

public class UserException extends RuntimeException {
    public UserException(int status, String message) {
        super(message);
    }
    public UserException(String message) {
        super(message);
    }
}