package com.hangout.core.hangoutauthservice.exceptions;

public class DTOConversionException extends MotherException {
    private final String dtoName;

    public DTOConversionException(String clientMessage, String dto) {
        super(clientMessage);
        this.dtoName = dto;
    }
}
