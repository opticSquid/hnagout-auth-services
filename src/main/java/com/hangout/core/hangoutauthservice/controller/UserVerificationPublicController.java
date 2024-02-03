package com.hangout.core.hangoutauthservice.controller;

import com.hangout.core.hangoutauthservice.service.UserVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auths/public")
@RequiredArgsConstructor
@Slf4j
public class UserVerificationPublicController {
    private final UserVerificationService userVerificationService;

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token) {
        this.userVerificationService.verifyUser(token);
        return "Account Verification process started! You will receive an email confirming your account verification";
    }
}
