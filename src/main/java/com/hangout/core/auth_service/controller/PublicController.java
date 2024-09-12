package com.hangout.core.auth_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hangout.core.auth_service.dto.request.ExistingUser;
import com.hangout.core.auth_service.dto.request.NewUser;
import com.hangout.core.auth_service.dto.response.AuthResponse;
import com.hangout.core.auth_service.dto.response.DefaultResponse;
import com.hangout.core.auth_service.service.UserDetailsServiceImpl;
import com.hangout.core.auth_service.utils.JwtUtils;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/public")
@Tag(name = "Public Endpoints")
@RequiredArgsConstructor
public class PublicController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signup")
    @Observed(name = "signup", contextualName = "controller")
    public ResponseEntity<DefaultResponse> signup(@RequestBody NewUser user) {
        try {
            userDetailsService.addNewUser(user);
            return new ResponseEntity<>(new DefaultResponse("Verification mail sent"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new DefaultResponse("User already exists"), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody ExistingUser user) {
        // ! if you put this code is UserDetailsService class AUthenticationManager will
        // ! have a circular dependency. So, let the code stay here
        try {
            String username = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(user.username(), user.password())).getName();
            String accessJwt = jwtUtils.generateAccessToken(username);
            String refreshJwt = jwtUtils.generateRefreshToken(username);
            return new ResponseEntity<>(new AuthResponse(accessJwt, refreshJwt), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new AuthResponse(null, null), HttpStatus.BAD_REQUEST);
        }
    }
}
