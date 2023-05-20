package com.demo.apptracky.exceptions;

public class InvalidJwtException extends RuntimeException {
    public InvalidJwtException(final String message) {
        super(message);
    }
}
