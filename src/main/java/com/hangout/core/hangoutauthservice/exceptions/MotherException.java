package com.hangout.core.hangoutauthservice.exceptions;

public class MotherException extends RuntimeException {
    public MotherException(String clientMessage) {
        super(clientMessage);
    }
}