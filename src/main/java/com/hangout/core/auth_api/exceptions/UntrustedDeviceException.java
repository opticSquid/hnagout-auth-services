package com.hangout.core.auth_api.exceptions;

public class UntrustedDeviceException extends RuntimeException {
    private String message;

    public UntrustedDeviceException() {
        super();
    }

    public UntrustedDeviceException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
