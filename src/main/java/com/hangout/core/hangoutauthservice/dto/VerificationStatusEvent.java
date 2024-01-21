package com.hangout.core.hangoutauthservice.dto;

public record VerificationStatusEvent(String email, Integer status) {
}
