package com.demo.apptracky.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidJwtErrorResponse extends BaseErrorResponse {
    private final boolean refreshJwt = true;

    public InvalidJwtErrorResponse(String message) {
        super(HttpStatus.UNAUTHORIZED.value(), message);
    }

    public boolean isRefreshJwt() {
        return refreshJwt;
    }
}
