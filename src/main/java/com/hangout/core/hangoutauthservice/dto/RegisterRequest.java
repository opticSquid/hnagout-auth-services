package com.hangout.core.hangoutauthservice.dto;

import com.hangout.core.hangoutauthservice.entity.Gender;

public record RegisterRequest(String name, String email, String password, Gender gender, Integer age) {
}
