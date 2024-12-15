package com.hangout.core.auth_api.exceptions;

public class UnIndentifiedDeviceException extends RuntimeException {
    private String message;

    public UnIndentifiedDeviceException() {
        super();
    }

    public UnIndentifiedDeviceException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
