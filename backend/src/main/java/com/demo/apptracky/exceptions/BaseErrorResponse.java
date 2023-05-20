package com.demo.apptracky.exceptions;

public class BaseErrorResponse {
    private int status;
    private String message;

    public BaseErrorResponse(final int status, final String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
