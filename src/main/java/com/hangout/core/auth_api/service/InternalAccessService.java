package com.hangout.core.auth_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hangout.core.auth_api.dto.request.PublicUserDetails;
import com.hangout.core.auth_api.dto.request.UserValidationRequest;

import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InternalAccessService {
    @Autowired
    private TokenValidityCheckerService tokenValidityCheckerService;

    @Observed(name = "check-token-validity", contextualName = "service")
    public PublicUserDetails checkTokenValidity(UserValidationRequest validationRequest) {
        return this.tokenValidityCheckerService.checkTokenValidity(validationRequest);
    }

}
