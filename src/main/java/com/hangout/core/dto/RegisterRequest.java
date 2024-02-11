package com.hangout.core.dto;

import com.hangout.core.entity.Gender;

public record RegisterRequest(String name, String email, String password, Gender gender, Integer age) {
}
