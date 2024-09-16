package com.hangout.core.auth_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hangout.core.auth_service.dto.response.DefaultResponse;
import com.hangout.core.auth_service.service.AccessService;
import com.hangout.core.auth_service.service.UserDetailsServiceImpl;

import io.micrometer.observation.annotation.Observed;
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
	public ResponseEntity<String> validateAccessToken() {
		// we really don't need to do anything here because
		// before the request reaches here jwt token is already validated by jwt filter
		// and access is recorded in db
		// if the request reaches till here this means everything is ok
		// cheeky right? 😜
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping("/logout")
	@Observed(name = "logout", contextualName = "controller")
	public ResponseEntity<DefaultResponse> logout(HttpServletRequest req) {
		Authentication user = getAuthenticatedUser();
		return new ResponseEntity<>(this.accessService.logout(user.getName(), req.getRemoteAddr()), HttpStatus.OK);
	}

	@DeleteMapping
	@Observed(name = "delete-account", contextualName = "controller")
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
