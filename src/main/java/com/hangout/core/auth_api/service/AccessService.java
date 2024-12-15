package com.hangout.core.auth_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hangout.core.auth_api.dto.request.DeviceDetails;
import com.hangout.core.auth_api.dto.request.ExistingUserCreds;
import com.hangout.core.auth_api.dto.request.PublicUserDetails;
import com.hangout.core.auth_api.dto.request.UserValidationRequest;
import com.hangout.core.auth_api.dto.response.AuthResponse;
import com.hangout.core.auth_api.dto.response.DefaultResponse;

import io.micrometer.observation.annotation.Observed;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccessService {
    @Autowired
    private LoginService loginService;
    @Autowired
    private RenewTokenService renewTokenService;
    @Autowired
    private TokenValidityCheckerService tokenValidityCheckerService;
    @Autowired
    private LogoutService logoutService;
    @Autowired
    private TrustDeviceService trustDeviceService;

    @Observed(name = "login", contextualName = "service")
    @Transactional
    public AuthResponse login(ExistingUserCreds userCreds, DeviceDetails deviceDetails) {
        return this.loginService.login(userCreds, deviceDetails);
    }

    @Observed(name = "renew-token", contextualName = "service")
    @Transactional
    public AuthResponse renewToken(String refreshToken, DeviceDetails deviceDetails) {
        return this.renewTokenService.renewToken(refreshToken, deviceDetails);

    }

    @Observed(name = "logout", contextualName = "service")
    public DefaultResponse logout(String accessToken, DeviceDetails deviceDetails) {
        return this.logoutService.logout(accessToken, deviceDetails);
    }

    @Observed(name = "check-token-validity", contextualName = "service")
    public PublicUserDetails checkTokenValidity(UserValidationRequest validationRequest) {
        return this.tokenValidityCheckerService.checkTokenValidity(validationRequest);
    }

    @Observed(name = "trust-device", contextualName = "service")
    public AuthResponse trustDevice(String accessToken, DeviceDetails deviceDetails) {
        return this.trustDeviceService.trustDevice(accessToken, deviceDetails);
    }
}
