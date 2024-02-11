package com.hangout.core.hangoutauthservice.controller;

import com.hangout.core.hangoutauthservice.service.UserVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Slf4j
public class UserVerificationPublicController {
    private final UserVerificationService userVerificationService;

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        this.userVerificationService.verifyUser(token);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type","text/html;charset=UTF-8");
        // hacking my way by forcing spring to send html response
        return ResponseEntity
                .ok()
                .headers(responseHeaders)
                .body("<h1>Account activation process has started, you will receive another mail confirming your account activation</h1>");
    }
}
