package com.hangout.core.auth_api.exceptions;

public class DeviceProfileException extends RuntimeException {
    private String message;

    public DeviceProfileException() {
        super();
    }

    public DeviceProfileException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
