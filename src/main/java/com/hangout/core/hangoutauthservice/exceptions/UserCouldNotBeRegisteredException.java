package com.hangout.core.hangoutauthservice.exceptions;

public class UserCouldNotBeRegisteredException extends MotherException{
    public UserCouldNotBeRegisteredException(String clientMessage) {
        super(clientMessage);
    }
}
