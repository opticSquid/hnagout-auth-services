package com.hangout.core.auth_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hangout.core.auth_api.dto.request.PublicUserDetails;
import com.hangout.core.auth_api.dto.response.DefaultResponse;
import com.hangout.core.auth_api.service.AccessService;
import com.hangout.core.auth_api.service.UserDetailsServiceImpl;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/user")
@Tag(name = "Protected Endpoints")
@Slf4j
public class UserController {
	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	@Autowired
	private AccessService accessService;

	@GetMapping("/validate")
	@Observed(name = "validate-token", contextualName = "controller")
	@Operation(summary = "check validity of access token")
	public ResponseEntity<PublicUserDetails> validateAccessToken(HttpServletRequest request) {
		return new ResponseEntity<>(
				this.accessService.checkTokenValidity(getAuthenticatedUser().getName(), request.getRemoteAddr()),
				HttpStatus.OK);
	}

	@DeleteMapping("/logout")
	@Observed(name = "logout", contextualName = "controller")
	@Operation(summary = "logout of an active session")
	public ResponseEntity<DefaultResponse> logout(HttpServletRequest req) {
		Authentication user = getAuthenticatedUser();
		return new ResponseEntity<>(this.accessService.logout(user.getName(), req.getRemoteAddr()), HttpStatus.OK);
	}

	@DeleteMapping
	@Observed(name = "delete-account", contextualName = "controller")
	@Operation(summary = "remove user account permanently")
	public ResponseEntity<DefaultResponse> deleteUser() {
		try {
			String userName = getAuthenticatedUser().getName();
			this.userDetailsService.deleteUser(userName);
			return new ResponseEntity<>(new DefaultResponse("User with username: " + userName + " deleted"),
					HttpStatus.OK);
		} catch (Exception e) {
			log.error("Exception: {}", e.getCause());
			return new ResponseEntity<>(new DefaultResponse("User could not be deleted"), HttpStatus.BAD_REQUEST);
		}
	}

	private Authentication getAuthenticatedUser() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
}
