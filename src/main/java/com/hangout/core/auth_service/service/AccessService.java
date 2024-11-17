package com.hangout.core.auth_service.service;

import java.math.BigInteger;
import java.time.Instant;
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

import com.hangout.core.auth_service.dto.request.ExistingUser;
import com.hangout.core.auth_service.dto.request.PublicUserDetails;
import com.hangout.core.auth_service.dto.response.AuthResponse;
import com.hangout.core.auth_service.dto.response.DefaultResponse;
import com.hangout.core.auth_service.entity.AccessRecord;
import com.hangout.core.auth_service.entity.Action;
import com.hangout.core.auth_service.entity.User;
import com.hangout.core.auth_service.exceptions.JwtNotValidException;
import com.hangout.core.auth_service.exceptions.UnauthorizedAccessException;
import com.hangout.core.auth_service.exceptions.UserNotFoundException;
import com.hangout.core.auth_service.repository.AccessRecordRepo;
import com.hangout.core.auth_service.repository.UserRepo;
import com.hangout.core.auth_service.utils.JwtUtil;

import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccessService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    @Qualifier("accessTokenUtil")
    private JwtUtil accessTokenUtil;
    @Autowired
    @Qualifier("refreshTokenUtil")
    private JwtUtil refreshTokenUtil;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AccessRecordRepo accessRecordRepo;

    @Observed(name = "login", contextualName = "service")
    public AuthResponse login(ExistingUser user, String ip) {
        log.debug("authenticating user: {}", user);
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.username(), user.password()));
        String username = auth.getName();
        log.debug("username from db:{}", username);
        // ** we are not checking if user exists or not here because earlier on we have
        // ** already checked that in authentication
        BigInteger userId = this.userRepo.findByUserName(username).get().getUserId();
        log.debug("user id from db: {}", userId);
        // get the last login/logout attempt by the user on the current device
        Optional<Instant> session = this.accessRecordRepo.getLastEntryAttemptRefTokenExpiry(userId, ip);
        Optional<Action> action = this.accessRecordRepo.getLastEntryAttemptAction(userId, ip);
        // ? condition 1:
        // if the user had a session in this device before (this is a known device for
        // the user)
        if (action.isPresent()) {
            // ? condition 1.1:
            // if the user is logging in after a long time it may happen
            // the user had not logged out of last session and refresh token of that session
            // has expired.
            // Allow the login in that case
            // and consider it as a new session
            log.debug("last entry action: {}, ref expiry: {}, current time: {}", action.get(),
                    session.get(), Instant.now());
            if (action.get().equals(Action.LOGIN)
                    && session.get().isBefore(Instant.now())) {
                log.debug("user is logging in after a long time, the past active session's refresh token has expired");
                String accessJwt = this.accessTokenUtil.generateToken(username);
                Date iatAccessToken = this.accessTokenUtil.getExpiresAt(accessJwt);
                String refreshJwt = this.refreshTokenUtil.generateToken(username);
                Date iatRefreshToken = this.refreshTokenUtil.getExpiresAt(refreshJwt);
                this.accessRecordRepo.save(
                        new AccessRecord(userId, ip, accessJwt, iatAccessToken, refreshJwt, iatRefreshToken, new Date(),
                                Action.LOGIN));
                return new AuthResponse(accessJwt, refreshJwt);
            }
            // ? condition 1.2:
            // ** if the user has an active session in the current device, do not
            // ** allow another login attempt (I donot need to check date here because if
            // ** refresh token was expired it would be dealt in above condition)
            else if (action.get().equals(Action.LOGIN)) {
                log.debug("User already has a active session in current device");
                throw new UnauthorizedAccessException(
                        "User already has an active session is this device. Either switch to the existing session or logout of the existing session");
            }
            // ? condition 1.3:
            // action will be logout if user had previously logged out of this account in
            // current device.
            else {
                log.debug("user had previously logged out of a session in this current device");
                String accessJwt = this.accessTokenUtil.generateToken(username);
                Date iatAccessToken = this.accessTokenUtil.getExpiresAt(accessJwt);
                String refreshJwt = this.refreshTokenUtil.generateToken(username);
                Date iatRefreshToken = this.refreshTokenUtil.getExpiresAt(refreshJwt);
                this.accessRecordRepo.save(
                        new AccessRecord(userId, ip, accessJwt, iatAccessToken, refreshJwt, iatRefreshToken, new Date(),
                                Action.LOGIN));
                return new AuthResponse(accessJwt, refreshJwt);
            }
        }
        // ? condition 3:
        // action would not be present if user has never logged in previously from
        // current device
        else {
            log.debug("action is not present");
            String accessJwt = this.accessTokenUtil.generateToken(username);
            Date iatAccessToken = this.accessTokenUtil.getExpiresAt(accessJwt);
            String refreshJwt = this.refreshTokenUtil.generateToken(username);
            Date iatRefreshToken = this.refreshTokenUtil.getExpiresAt(refreshJwt);
            this.accessRecordRepo.save(
                    new AccessRecord(userId, ip, accessJwt, iatAccessToken, refreshJwt, iatRefreshToken, new Date(),
                            Action.LOGIN));
            return new AuthResponse(accessJwt, refreshJwt);
        }
    }

    @Observed(name = "renew-token", contextualName = "service")
    public AuthResponse renewToken(String refreshToken, String ip) {
        // ? 1 Validate refresh token
        // ? 1.1 If valid
        // ? 1.1.1 Get the username from token
        // ? 1.1.2 find the userId from user_creds table
        // ? 1.1.2.1 if found
        // ? 1.1.2.1.1 Take that userId and current ip address and try to find the
        // ? latest record from access_record
        // ? 1.1.2.1.1 if found
        // ? 1.1.2.1.1.1 Take the access token, access token issue time, refresh token
        // ? from there
        // ? 1.1.2.1.1.2 Check if access token was expired from the access token issue
        // ? time
        // ? 1.1.2.1.1.3.1 If expired create a new access token
        // ? 1.1.2.1.1.3.2 If not assign the existing access token to return object
        // ? 1.1.2.1.1.4 Register a hearbeat
        // ? 1.1.2.1.1.5 Return AuthResponse
        // ? 1.1.2.1.2 if not found
        // ? 1.1.2.1.2.1 return unauthorized access because user needs to login first in
        // ? that device to renew token
        // ? 1.1.2.2 if user not found in user_creds
        // ? 1.1.2.2.1 return user not found exception
        // ? 1.2 if jwt is not valid
        // ? 1.2.1 return jwt not valid exception
        log.debug("refreshToken: {}, ip: {}", refreshToken, ip);
        if (this.refreshTokenUtil.validateToken(refreshToken)) {
            String username = this.refreshTokenUtil.getUsername(refreshToken);
            log.debug("username extracted from token: {}", username);
            Optional<User> user = this.userRepo.findByUserName(username);
            if (user.isPresent() && user.get().isEnabled()) {
                log.debug("user found from db: {}", user.get());
                Optional<AccessRecord> latestAccess = this.accessRecordRepo.getLatestAccess(user.get().getUserId(), ip);
                log.debug("is latest access record present: {}", latestAccess.isPresent());
                // Latest access record is present and the latest access record is not logout
                if (latestAccess.isPresent() && !latestAccess.get().getAction().equals(Action.LOGOUT)) {
                    // check if access token expiry time (UTC time) is before current UTC time
                    // this means access token is expired we need to create a new one
                    log.debug("Access token expiry time: {}", latestAccess.get().getAccessTokenExpiryTime());
                    log.debug("Current UTC time: {}", ZonedDateTime.now(ZoneOffset.UTC));
                    log.info("Is token expired: {}", latestAccess.get().getAccessTokenExpiryTime()
                            .isBefore(ZonedDateTime.now(ZoneOffset.UTC)));
                    if (latestAccess.get().getAccessTokenExpiryTime()
                            .isBefore(ZonedDateTime.now(ZoneOffset.UTC))) {
                        String accessToken = this.accessTokenUtil.generateToken(username);
                        ZonedDateTime expiryTime = this.accessTokenUtil.getExpiresAt(accessToken).toInstant()
                                .atZone(ZoneOffset.UTC);
                        this.accessRecordRepo.save(new AccessRecord(user.get().getUserId(), ip, accessToken,
                                expiryTime, latestAccess.get().getRefreshToken(),
                                latestAccess.get().getRefreshTokenExpiryTime(), new Date(), Action.RENEW_TOKEN));
                        return new AuthResponse(accessToken, refreshToken);
                    } else {
                        String accessToken = latestAccess.get().getAccessToken();
                        this.accessRecordRepo.save(new AccessRecord(user.get().getUserId(), ip, accessToken,
                                latestAccess.get().getAccessTokenExpiryTime(), latestAccess.get().getRefreshToken(),
                                latestAccess.get().getRefreshTokenExpiryTime(), new Date(),
                                Action.PREMATURE_TOKEN_RENEW));
                        return new AuthResponse(accessToken, refreshToken);
                    }
                } else {
                    throw new UnauthorizedAccessException(
                            "No login attempt was made from this device previously. So, can not initiate a new session");
                }
            } else {
                throw new UserNotFoundException("User indicated by the token was not found");
            }
        } else {
            throw new JwtNotValidException("Token provided is invalid");
        }
    }

    @Observed(name = "logout", contextualName = "service")
    public DefaultResponse logout(String userName, String ip) {
        Optional<User> user = this.userRepo.findByUserName(userName);
        if (user.isPresent()) {
            Optional<AccessRecord> access = this.accessRecordRepo.getLatestAccess(user.get().getUserId(), ip);
            if (access.isPresent()) {
                this.accessRecordRepo.save(new AccessRecord(user.get().getUserId(), ip, access.get().getAccessToken(),
                        access.get().getAccessTokenExpiryTime(), access.get().getRefreshToken(),
                        access.get().getRefreshTokenExpiryTime(), new Date(), Action.LOGOUT));
                return new DefaultResponse("Successfully Logged out from this device");
            } else {
                throw new UnauthorizedAccessException("There is no active sesion in the current device");
            }

        } else {
            // this will never occur because if the user did not exist in db they would not
            // have been authenticated in first place
            // thus not allowed to access this route
            // but still keeping it just in case
            throw new UserNotFoundException("Current user is not found in database");
        }
    }

    public PublicUserDetails checkTokenValidity(String username, String ip) {
        // get user from database
        log.debug("Username: {}, ip: {}", username, ip);
        // ! for dev only
        ip = ip.equals("127.0.0.1") ? "0:0:0:0:0:0:0:1" : ip;
        Optional<User> user = this.userRepo.findByUserName(username);
        if (user.isPresent() && user.get().isEnabled()) {
            Optional<AccessRecord> latestAccess = this.accessRecordRepo.getLatestAccess(user.get().getUserId(), ip);
            // check if the last action of the user in the given device was not logout
            if (latestAccess.isPresent() && !latestAccess.get().getAction().equals(Action.LOGOUT)) {
                return new PublicUserDetails(username, user.get().getRole());
            } else {
                throw new UnauthorizedAccessException("User is not authorized to access this route");
            }
        } else {
            throw new UserNotFoundException("User indicated by the token was not found");
        }
    }
}
