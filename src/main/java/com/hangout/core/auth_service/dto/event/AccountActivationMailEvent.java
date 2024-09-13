package com.hangout.core.auth_service.dto.event;

public record AccountActivationMailEvent(String name, String email, Integer status) {

}
