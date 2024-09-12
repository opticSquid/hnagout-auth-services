package com.hangout.core.auth_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hangout.core.auth_service.dto.request.ExistingUser;
import com.hangout.core.auth_service.dto.request.NewUser;
import com.hangout.core.auth_service.dto.response.AuthResponse;
import com.hangout.core.auth_service.dto.response.DefaultResponse;
import com.hangout.core.auth_service.service.AccessService;
import com.hangout.core.auth_service.service.UserDetailsServiceImpl;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/public")
@Tag(name = "Public Endpoints")
@RequiredArgsConstructor
public class PublicController {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private AccessService accessService;

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
        try {
            AuthResponse res = this.accessService.login(user);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new AuthResponse(null, null), HttpStatus.BAD_REQUEST);
        }
    }
}
