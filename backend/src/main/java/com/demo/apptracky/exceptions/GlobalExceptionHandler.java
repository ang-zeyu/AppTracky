package com.demo.apptracky.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler
    public ResponseEntity<InvalidJwtErrorResponse> invalidJwt(final InvalidJwtException ex) {
        final InvalidJwtErrorResponse resp = new InvalidJwtErrorResponse(ex.getMessage());
        return new ResponseEntity<>(resp, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    public ResponseEntity<BaseErrorResponse> responseStatusEx(final ResponseStatusException ex) {
        final BaseErrorResponse resp = new BaseErrorResponse(ex.getStatusCode().value(), ex.getReason());
        return new ResponseEntity<>(resp, ex.getStatusCode());
    }

    @ExceptionHandler
    public ResponseEntity<BaseErrorResponse> internalServerError(final RuntimeException ex) {
        log.error("Unhandled error occured", ex);
        final BaseErrorResponse resp = new BaseErrorResponse(500, "Internal server error!");
        return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
