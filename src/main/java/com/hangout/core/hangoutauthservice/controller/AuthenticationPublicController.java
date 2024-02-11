package com.hangout.core.hangoutauthservice.controller;

import com.hangout.core.hangoutauthservice.config.MessageProducer;
import com.hangout.core.hangoutauthservice.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hangout.core.hangoutauthservice.service.AuthenticationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationPublicController {
	private final AuthenticationService authService;

	@PostMapping("/register")
	public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
		return ResponseEntity.ok(authService.registerAsNonVerifiedUser(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
		return ResponseEntity.ok(authService.authenticate(request));
	}

}
