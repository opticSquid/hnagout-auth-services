package com.hangout.core.hangoutauthservice.controller;

import com.hangout.core.hangoutauthservice.dto.AuthenticationResponse;
import com.hangout.core.hangoutauthservice.dto.RenewAccessTokenRequest;
import com.hangout.core.hangoutauthservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auths/authenticated")
@RequiredArgsConstructor
@Slf4j
public class AuthenticatedController {
    private final AuthenticationService authService;
    @PostMapping("/renew-token")
    public ResponseEntity<AuthenticationResponse> renewAccessToken(@RequestBody RenewAccessTokenRequest request) {
        AuthenticationResponse newTokens = authService.renewToken(request);
        return new ResponseEntity<AuthenticationResponse>(newTokens, HttpStatusCode.valueOf(200));
    }
}
