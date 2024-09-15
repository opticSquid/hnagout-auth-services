package com.hangout.core.auth_service.exceptions;

public class EmailOrPasswordWrongException extends RuntimeException {
	private static final long serialVersionUID = -5369312191560192519L;
	private String message;

	public EmailOrPasswordWrongException() {
		super();
	}

	public EmailOrPasswordWrongException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
