package com.hangout.core.hangoutauthservice.exceptions;

public class EmailOrPasswordWrong extends RuntimeException {
	private static final long serialVersionUID = -5369312191560192519L;
	private String message;
	public EmailOrPasswordWrong() {
		super();
	}
	public EmailOrPasswordWrong(String message) {
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
	
}
