package com.hangout.core.hangoutauthservice.controller;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hangout.core.hangoutauthservice.dto.AuthenticationRequest;
import com.hangout.core.hangoutauthservice.dto.AuthenticationResponse;
import com.hangout.core.hangoutauthservice.dto.RegisterRequest;
import com.hangout.core.hangoutauthservice.dto.RenewAccessTokenRequest;
import com.hangout.core.hangoutauthservice.dto.UserNameResponse;
import com.hangout.core.hangoutauthservice.dto.UserNamesRequest;
import com.hangout.core.hangoutauthservice.dto.ValidateRequest;
import com.hangout.core.hangoutauthservice.dto.ValidateResponse;
import com.hangout.core.hangoutauthservice.exceptions.UserNotFoundException;
import com.hangout.core.hangoutauthservice.service.AuthenticationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auths/auth")
@RequiredArgsConstructor
public class AuthenticationController {
	private final AuthenticationService authService;

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
		return ResponseEntity.ok(authService.authenticate(request));
	}

	@PostMapping("/change-role")
	public ResponseEntity<Boolean> changeRole(@RequestBody String userId) {
		try {
			Boolean result = authService.changeRoleToBusinessOwner(userId);
			if (result) {
				return ResponseEntity.status(HttpStatus.SC_OK).body(result);
			} else {
				return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(result);
			}
		} catch (UserNotFoundException e) {
			return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).body(false);
		}
	}

	@PostMapping("/validate")
	public ResponseEntity<ValidateResponse> validateJwt(@RequestBody ValidateRequest request) {
		ValidateResponse user = authService.validateUser(request.token());
		if (user.getMessage().equals("jwt expired") || user.getMessage().equals("the given jwt string is not valid")) {
			return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(user);
		} else {
			return ResponseEntity.status(HttpStatus.SC_OK).body(user);
		}
	}

	@PostMapping("/renew-token")
	public ResponseEntity<AuthenticationResponse> renewAccessToken(@RequestBody RenewAccessTokenRequest request) {
		AuthenticationResponse newTokens = authService.renewToken(request);
		return new ResponseEntity<AuthenticationResponse>(newTokens, HttpStatusCode.valueOf(200));
	}

	@PostMapping("/get-usernames")
	public ResponseEntity<UserNameResponse> getUserNames(@RequestBody UserNamesRequest listOfUserIds) {
		UserNameResponse userNames = authService.getUserNamesForUserIds(listOfUserIds.listOfUserIds());
		return new ResponseEntity<UserNameResponse>(userNames, HttpStatusCode.valueOf(200));
	}
}
