package com.hangout.core.auth_service.exceptions;

public class UserNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 4793098800796683556L;
	private String message;

	public UserNotFoundException() {
		super();
	}

	public UserNotFoundException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
