package com.hangout.core.hangoutauthservice.dto;

public record NewVerifiedUserEvent(String email, Boolean verificationStatus) {
}
