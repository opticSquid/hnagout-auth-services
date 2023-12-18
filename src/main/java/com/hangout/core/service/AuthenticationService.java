package com.hangout.core.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hangout.core.dto.AuthenticationRequest;
import com.hangout.core.dto.AuthenticationResponse;
import com.hangout.core.dto.RegisterRequest;
import com.hangout.core.dto.RenewAccessTokenRequest;
import com.hangout.core.dto.UserNameResponse;
import com.hangout.core.dto.ValidateResponse;
import com.hangout.core.entity.Role;
import com.hangout.core.entity.User;
import com.hangout.core.exceptions.EmailOrPasswordWrong;
import com.hangout.core.exceptions.JwtNotValidException;
import com.hangout.core.exceptions.UserNotFoundException;
import com.hangout.core.repository.UserNameProjection;
import com.hangout.core.repository.UserRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

	private final UserRepo repo;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthenticationResponse register(RegisterRequest request) {
		User user = User.builder().email(request.email()).name(request.name())
				.password(passwordEncoder.encode(request.password())).role(Role.USER)
				.gender(request.gender()).age(request.age())
				.build();
		repo.save(user);
		String accessToken = jwtService.generateToken(user, "access");
		String refreshToken = jwtService.generateToken(user, "refresh");
		return AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
	}

	public Boolean changeRoleToBusinessOwner(String userId) {
		User user = repo.findById(userId).orElseThrow(() -> new UserNotFoundException("user with given id not found"));
		user.setRole(Role.SERVICE_OWNER);
		try {
			repo.save(user);
			return true;
		} catch (Exception e) {
			return false;
		}

	}

	public AuthenticationResponse authenticate(AuthenticationRequest request) {
		try {
			authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
		} catch (BadCredentialsException ex) {
			throw new EmailOrPasswordWrong("email or password is wrong");
		}
		User user = repo.findByEmail(request.email())
				.orElseThrow(() -> new UserNotFoundException("user with given email not found"));
		String accessToken = jwtService.generateToken(user, "access");
		String refreshToken = jwtService.generateToken(user, "refresh");
		return AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
	}

	public ValidateResponse validateUser(String token) {
		try {
			String isValidUserEmail = jwtService.checkIfTokenValid(token, "access");
			log.error("email returned from jwtService: {}", isValidUserEmail);
			if (isValidUserEmail != null) {
				User user = repo.findByEmail(isValidUserEmail)
						.orElseThrow(() -> new UserNotFoundException("user with given email not found"));
				return new ValidateResponse(user.getUserId(), user.getRole(), "user validated");
			} else {
				return new ValidateResponse(null, null, "jwt expired or mismatched");
			}
		} catch (JwtNotValidException e) {
			return new ValidateResponse(null, null, e.getMessage());
		}
	}

	public AuthenticationResponse renewToken(RenewAccessTokenRequest accessRequest) {
		try {
			String isValidUserEmail = jwtService.checkIfTokenValid(accessRequest.refreshToken(), "refresh");
			log.error("email returned from jwtService: {}", isValidUserEmail);
			if (isValidUserEmail != null) {
				User user = repo.findByEmail(isValidUserEmail)
						.orElseThrow(() -> new UserNotFoundException("user with given email not found"));
				String accessToken = jwtService.generateToken(user, "access");
				return AuthenticationResponse.builder().accessToken(accessToken)
						.refreshToken(accessRequest.refreshToken()).build();
			} else {
				throw new EmailOrPasswordWrong("incoming user is not found");
			}
		} catch (JwtNotValidException e) {
			return new AuthenticationResponse(null, accessRequest.refreshToken());
		}
	}

	public UserNameResponse getUserNamesForUserIds(List<String> listOfUserIds) {
		try {
			log.error("user names: {}", repo.findByUserIdIn(listOfUserIds));
			List<String> userNames = repo.findByUserIdIn(listOfUserIds).stream().map(UserNameProjection::getName)
					.collect(Collectors.toList());

			return UserNameResponse.builder().userNames(userNames).build();
		} catch (Exception e) {
			throw new UserNotFoundException("user with given ids not found in the database");
		}
	}
}
