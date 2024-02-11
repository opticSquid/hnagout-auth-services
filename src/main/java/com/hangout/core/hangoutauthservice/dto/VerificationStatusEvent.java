package com.hangout.core.hangoutauthservice.dto;

public record VerificationStatusEvent(String email, String name, Integer status) {
}
