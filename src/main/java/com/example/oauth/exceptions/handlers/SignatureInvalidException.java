package com.example.oauth.exceptions.handlers;

public class SignatureInvalidException extends RuntimeException {
    public SignatureInvalidException(String message) {
        super(message);
    }
}
