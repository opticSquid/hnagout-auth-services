package com.hangout.core.auth_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import com.hangout.core.auth_api.dto.request.PublicUserDetails;
import com.hangout.core.auth_api.service.AccessService;

@RestController
@RequestMapping("/v1/internal")
@Tag(name = "Internal Endpoints")
@Slf4j
public class InternalServiceController {
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

    private Authentication getAuthenticatedUser() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
