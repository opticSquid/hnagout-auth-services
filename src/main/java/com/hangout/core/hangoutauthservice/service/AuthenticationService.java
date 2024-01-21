package com.hangout.core.hangoutauthservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hangout.core.hangoutauthservice.config.MessageProducer;
import com.hangout.core.hangoutauthservice.dto.*;
import com.hangout.core.hangoutauthservice.entity.Authorization;
import com.hangout.core.hangoutauthservice.entity.Role;
import com.hangout.core.hangoutauthservice.entity.User;
import com.hangout.core.hangoutauthservice.exceptions.EmailOrPasswordWrong;
import com.hangout.core.hangoutauthservice.exceptions.JwtNotValidException;
import com.hangout.core.hangoutauthservice.exceptions.UserCouldNotBeRegisteredException;
import com.hangout.core.hangoutauthservice.exceptions.UserNotFoundException;
import com.hangout.core.hangoutauthservice.repository.UserNameProjection;
import com.hangout.core.hangoutauthservice.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepo repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MessageProducer messageProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RegisterResponse registerAsNonVerifiedUser(RegisterRequest request) {
        User newUser = new User(request.name(), request.age(), request.gender(), request.email(), passwordEncoder.encode(request.password()), Role.USER, Authorization.USER, false, false, false, false);
        try {
            repo.save(newUser);
            messageProducer.sendMessage("new-user-registered", objectMapper.writeValueAsString(new NewUnverifiedUserEvent(newUser.getEmail())));
            return new RegisterResponse("An email has been sent to " + request.email() + " please click the verification link to verify your account");
        } catch (Exception ex) {
            throw new UserCouldNotBeRegisteredException("The user could not be registered because an account already exists with this email: " + request.email());
        }
//        String accessToken = jwtService.generateToken(newUser, "access");
//        String refreshToken = jwtService.generateToken(newUser, "refresh");
        //        return new AuthenticationResponse(accessToken, refreshToken);
    }

    /**
     * This function is fired when kafka event is received Via Message Consumer
     * @see com.hangout.core.hangoutauthservice.config.MessageConsumer
     * @param verifiedUserEvent
     */
    @Transactional
    public void userVerified(NewVerifiedUserEvent verifiedUserEvent) {
        Optional<User> unverifiedUserFromDB = repo.findByEmail(verifiedUserEvent.email());
        try {
            if (unverifiedUserFromDB.isPresent()) {
                User toBeVerifiedUser = unverifiedUserFromDB.get();
                toBeVerifiedUser.setIsEnabled(true);
                repo.save(toBeVerifiedUser);
                messageProducer.sendMessage("verification-status", objectMapper.writeValueAsString(new VerificationStatusEvent(verifiedUserEvent.email(), 200)));
            } else {
                messageProducer.sendMessage("verification-status", objectMapper.writeValueAsString(new VerificationStatusEvent(verifiedUserEvent.email(), 500)));
            }
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("POJO classes could not be converted to JSON");
        }
    }

    public boolean changeAuthorizationToPV(String userId) {
        boolean result;
        User user = repo.findById(userId).orElseThrow(() -> new UserNotFoundException("user with given id not found"));
        user.setAuthorization(Authorization.PV);
        try {
            repo.save(user);
            result = true;
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException ex) {
            throw new EmailOrPasswordWrong("email or password is wrong");
        }
        User user = repo.findByEmail(request.email()).orElseThrow(() -> new UserNotFoundException("user with given email not found"));
        String accessToken = jwtService.generateToken(user, "access");
        String refreshToken = jwtService.generateToken(user, "refresh");
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public ValidateResponse validateUser(String token) {
        try {
            String isValidUserEmail = jwtService.checkIfTokenValid(token, "access");
            log.error("email returned from jwtService: {}", isValidUserEmail);
            if (isValidUserEmail != null) {
                User user = repo.findByEmail(isValidUserEmail).orElseThrow(() -> new UserNotFoundException("user with given email not found"));
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
                User user = repo.findByEmail(isValidUserEmail).orElseThrow(() -> new UserNotFoundException("user with given email not found"));
                String accessToken = jwtService.generateToken(user, "access");
                return new AuthenticationResponse(accessToken, accessRequest.refreshToken());
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
            List<String> userNames = repo.findByUserIdIn(listOfUserIds).stream().map(UserNameProjection::getName).collect(Collectors.toList());

            return UserNameResponse.builder().userNames(userNames).build();
        } catch (Exception e) {
            throw new UserNotFoundException("user with given ids not found in the database");
        }
    }
}
