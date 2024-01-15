package com.hangout.core.hangoutauthservice.controller;

import com.hangout.core.hangoutauthservice.config.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hangout.core.hangoutauthservice.dto.AuthenticationRequest;
import com.hangout.core.hangoutauthservice.dto.AuthenticationResponse;
import com.hangout.core.hangoutauthservice.dto.RegisterRequest;
import com.hangout.core.hangoutauthservice.dto.RenewAccessTokenRequest;
import com.hangout.core.hangoutauthservice.service.AuthenticationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auths/public")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationPublicController {
	private final AuthenticationService authService;
	private final MessageProducer messageProducer;

	@PostMapping("/send")
	public String sendMessage(@RequestParam("message") String message) {
		log.info("request received for kafka");
		messageProducer.sendMessage("my-topic", message);
		return "Message sent: " + message;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
		return ResponseEntity.ok(authService.registerAsNonVerifiedUser(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
		return ResponseEntity.ok(authService.authenticate(request));
	}

}
