package com.hangout.core.auth_service.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.hangout.core.auth_service.exceptions.JwtNotValidException;
import com.hangout.core.auth_service.exceptions.UnauthorizedAccessException;
import com.hangout.core.auth_service.exceptions.UserNotFoundException;

import io.jsonwebtoken.MalformedJwtException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	@ExceptionHandler(UserNotFoundException.class)
	public ProblemDetail exceptionHandler(UserNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("Given user/s not found");
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

	// Spring generated exceptions

	@ExceptionHandler(BadCredentialsException.class)
	public ProblemDetail exceptionHandler(BadCredentialsException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
		problem.setTitle("Username or password is wrong");
		return problem;
	}

	@ExceptionHandler(MalformedJwtException.class)
	public ProblemDetail exceptionHandler(MalformedJwtException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
		problem.setTitle("Token is invalid");
		return problem;
	}

}