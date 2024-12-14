package com.hangout.core.auth_api.service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.hangout.core.auth_api.dto.request.DeviceDetails;
import com.hangout.core.auth_api.dto.request.ExistingUserCreds;
import com.hangout.core.auth_api.dto.response.AuthResponse;
import com.hangout.core.auth_api.entity.AccessRecord;
import com.hangout.core.auth_api.entity.Action;
import com.hangout.core.auth_api.entity.Device;
import com.hangout.core.auth_api.entity.User;
import com.hangout.core.auth_api.exceptions.UntrustedDeviceException;
import com.hangout.core.auth_api.exceptions.UnauthorizedAccessException;
import com.hangout.core.auth_api.repository.AccessRecordRepo;
import com.hangout.core.auth_api.repository.DeviceRepo;
import com.hangout.core.auth_api.repository.UserRepo;
import com.hangout.core.auth_api.utils.DeviceUtil;
import com.hangout.core.auth_api.utils.JwtUtil;
import com.hangout.core.auth_api.utils.RefreshTokenUtil;

import io.micrometer.observation.annotation.Observed;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
class LoginService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    @Qualifier("accessTokenUtil")
    private JwtUtil accessTokenUtil;
    @Autowired
    @Qualifier("refreshTokenUtil")
    private RefreshTokenUtil refreshTokenUtil;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AccessRecordRepo accessRecordRepo;
    @Autowired
    private DeviceRepo deviceRepo;
    @Autowired
    private DeviceUtil deviceUtils;

    @Observed(name = "login", contextualName = "login-service")
    @Transactional
    public AuthResponse login(ExistingUserCreds userCreds, DeviceDetails deviceDetails) {
        log.debug("authenticating user");
        Authentication auth = autheticateUser(userCreds);
        String username = auth.getName();
        log.debug("authenticated user : {}", username);
        User user = this.userRepo.findByUserName(username).get();
        log.debug("user id from db: {}", user.getUserId());
        // if user is not enabled, do not allow login
        if (!user.isEnabled()) {
            return new AuthResponse(null, null, "user not enabled");
        } else {
            // user is enabled
            // check if the current device is a trusted device or not
            try {
                Device trustedDevice = getUserDevice(deviceDetails, user);
                // get the last login/logout attempt by the user in current device
                Optional<AccessRecord> session = this.accessRecordRepo.getLastEntryRecord(user.getUserId(),
                        trustedDevice.getDeviceId());
                // user had logged in before in the current device and marked it as a trusted
                // device
                return userLoggingInToTrustedDevice(session.get(), trustedDevice, user);
            } catch (UntrustedDeviceException ex) {
                log.info("user logging in a new device");
                Device newUntrustedDevice = this.deviceUtils.getDevice(deviceDetails, user);
                return userLoggingInAUntrustedDevice(newUntrustedDevice, user);
            }
        }
    }

    private Authentication autheticateUser(ExistingUserCreds userCreds) {
        return authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(userCreds.username(), userCreds.password()));
    }

    private Device getUserDevice(DeviceDetails deviceDetails, User user) {
        Device currentDevice = deviceUtils.getDevice(deviceDetails, user);
        Optional<Device> deviceFromDb = this.deviceRepo.findDevice(deviceDetails.screenWidth(),
                deviceDetails.screenHeight(), deviceDetails.os(), deviceDetails.userAgent(), currentDevice.getCountry(),
                user.getUserId());
        if (deviceFromDb.isPresent()) {
            return deviceFromDb.get();
        } else {
            throw new UntrustedDeviceException("Device was never used by user before");
        }
    }

    private AuthResponse userLoggingInToTrustedDevice(AccessRecord session,
            Device trustedDevice, User user) {
        Action action = session.getAction();
        ZonedDateTime refreshTokenExpiryTime = session.getRefreshTokenExpiryTime();
        if (action.equals(Action.LOGIN)
                && refreshTokenExpiryTime.isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
            log.info("user is logging in after a long time, the past active session's refresh token has expired");
            return loginAfterRefreshTokenExpired(trustedDevice, user);
        } else if (action.equals(Action.LOGIN)) {
            log.info("User already has a active session in current device");
            throw new UnauthorizedAccessException(
                    "User already has an active session is this device. Either switch to the existing session or logout of the existing session");
        } else {
            return loginAfterUserManuallyLoggedOut(trustedDevice, user);
        }
    }

    private AuthResponse loginAfterRefreshTokenExpired(Device trustedDevice, User user) {
        String accessJwt = this.accessTokenUtil.generateToken(user.getUsername(), trustedDevice.getDeviceId());
        Date iatAccessToken = this.accessTokenUtil.getExpiresAt(accessJwt);
        String refreshJwt = this.refreshTokenUtil.generateToken(user.getUsername(), trustedDevice.getDeviceId());
        Date iatRefreshToken = this.refreshTokenUtil.getExpiresAt(refreshJwt);
        AccessRecord accessRecord = this.accessRecordRepo.save(
                new AccessRecord(accessJwt, iatAccessToken, refreshJwt, iatRefreshToken, Action.LOGIN,
                        trustedDevice, user));
        user.addAccessRecord(accessRecord);
        trustedDevice.addAccessRecord(accessRecord);
        this.deviceRepo.save(trustedDevice);
        this.userRepo.save(user);
        return new AuthResponse(accessJwt, refreshJwt, "success");
    }

    private AuthResponse loginAfterUserManuallyLoggedOut(
            Device trustedDevice, User user) {
        log.info("user had previously logged out of a session in this current device");
        String accessJwt = this.accessTokenUtil.generateToken(user.getUsername(), trustedDevice.getDeviceId());
        Date iatAccessToken = this.accessTokenUtil.getExpiresAt(accessJwt);
        String refreshJwt = this.refreshTokenUtil.generateToken(user.getUsername(), trustedDevice.getDeviceId());
        Date iatRefreshToken = this.refreshTokenUtil.getExpiresAt(refreshJwt);
        AccessRecord accessRecord = this.accessRecordRepo.save(
                new AccessRecord(accessJwt, iatAccessToken, refreshJwt, iatRefreshToken, Action.LOGIN,
                        trustedDevice, user));
        user.addAccessRecord(accessRecord);
        trustedDevice.addAccessRecord(accessRecord);
        this.deviceRepo.save(trustedDevice);
        this.userRepo.save(user);
        return new AuthResponse(accessJwt, refreshJwt, "success");
    }

    private AuthResponse userLoggingInAUntrustedDevice(Device newDevice, User user) {
        // saving new Device
        newDevice = this.deviceRepo.save(newDevice);
        String accessJwt = this.accessTokenUtil.generateToken(user.getUsername(), newDevice.getDeviceId());
        Date iatAccessToken = this.accessTokenUtil.getExpiresAt(accessJwt);
        String shortTermRefreshJwt = this.refreshTokenUtil.generateTokenShortTerm(user.getUsername(),
                newDevice.getDeviceId());
        Date iatRefreshToken = this.refreshTokenUtil.getExpiresAt(shortTermRefreshJwt);
        AccessRecord accessRecord = new AccessRecord(accessJwt, iatAccessToken, shortTermRefreshJwt, iatRefreshToken,
                Action.LOGIN,
                newDevice, user);
        log.debug("new access record: {}", accessRecord);
        accessRecord = this.accessRecordRepo.save(accessRecord);
        user.addAccessRecord(accessRecord);
        newDevice.addAccessRecord(accessRecord);
        this.deviceRepo.save(newDevice);
        this.userRepo.save(user);
        return new AuthResponse(accessJwt, shortTermRefreshJwt, "untrusted device login");
    }

}
