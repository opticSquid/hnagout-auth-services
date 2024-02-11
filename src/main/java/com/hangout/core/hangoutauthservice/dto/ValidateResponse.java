package com.hangout.core.hangoutauthservice.dto;

import com.hangout.core.hangoutauthservice.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateResponse {
	private String userId;
	private Role role;
	private String message;

	public ValidateResponse(String userId, Role role) {
		this.userId = userId;
		this.role = role;
	}
}
