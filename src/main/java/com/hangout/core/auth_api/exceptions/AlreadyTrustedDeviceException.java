package com.hangout.core.auth_api.exceptions;

public class AlreadyTrustedDeviceException extends RuntimeException {
    private String message;

    public AlreadyTrustedDeviceException() {
        super();
    }

    public AlreadyTrustedDeviceException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
