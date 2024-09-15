package com.hangout.core.auth_service.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.hangout.core.auth_service.exceptions.EmailOrPasswordWrongException;
import com.hangout.core.auth_service.exceptions.JwtNotValidException;
import com.hangout.core.auth_service.exceptions.UnauthorizedAccessException;
import com.hangout.core.auth_service.exceptions.UserNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	@ExceptionHandler(UserNotFoundException.class)
	public ProblemDetail exceptionHandler(UserNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("Given user/s not found");
		return problem;
	}

	@ExceptionHandler(EmailOrPasswordWrongException.class)
	public ProblemDetail exceptionHandler(EmailOrPasswordWrongException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setTitle("Email or Password is Wrong");
		return problem;
	}

	@ExceptionHandler(JwtNotValidException.class)
	public ProblemDetail exceptionHandler(JwtNotValidException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setTitle("Token is invalid");
		return problem;
	}

	@ExceptionHandler(UnauthorizedAccessException.class)
	public ProblemDetail exceptionHandler(UnauthorizedAccessException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
		problem.setTitle("Access Denied");
		return problem;
	}

}