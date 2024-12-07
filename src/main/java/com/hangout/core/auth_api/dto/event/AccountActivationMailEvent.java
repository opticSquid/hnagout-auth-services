package com.hangout.core.auth_api.dto.event;

public record AccountActivationMailEvent(String name, String email, Integer status) {

}
