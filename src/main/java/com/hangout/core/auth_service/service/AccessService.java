package com.hangout.core.auth_service.service;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import com.hangout.core.auth_service.dto.response.AuthResponse;
import com.hangout.core.auth_service.entity.AccessRecord;
import com.hangout.core.auth_service.entity.Action;
import com.hangout.core.auth_service.entity.User;
import com.hangout.core.auth_service.exceptions.JwtNotValidException;
import com.hangout.core.auth_service.exceptions.UnauthorizedAccessException;
import com.hangout.core.auth_service.exceptions.UserNotFoundException;
import com.hangout.core.auth_service.repository.AccessRecordRepo;
import com.hangout.core.auth_service.repository.UserRepo;
import com.hangout.core.auth_service.utils.JwtUtil;

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

    public AuthResponse login(ExistingUser user, String ip) throws Exception {
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.username(), user.password()));
        String username = auth.getName();
        String accessJwt = this.accessTokenUtil.generateToken(username);
        Date iatAccessToken = this.accessTokenUtil.getExpiresAt(accessJwt);
        String refreshJwt = this.refreshTokenUtil.generateToken(username);
        Date iatRefreshToken = this.refreshTokenUtil.getExpiresAt(refreshJwt);
        // ? we are not checking if user exists or not here because earlier on we have
        // ? already checked that in authentication
        BigInteger userId = this.userRepo.findByUserName(username).get().getUserId();
        // saving login attempt as new record of access
        this.accessRecordRepo.save(
                new AccessRecord(userId, ip, accessJwt, iatAccessToken, refreshJwt, iatRefreshToken, new Date(),
                        Action.LOGIN));
        return new AuthResponse(accessJwt, refreshJwt);
    }

    public AuthResponse renewToken(String refreshToken, String ip) {
        // ? 1 Validate refresh token
        // ? 1.1 If valid
        // ? 1.1.1 Get the username from token
        // ? 1.1.2 find the userId from user_creds table
        // ? 1.1.2.1 if found
        // ? 1.1.2.1.1 Take that userId and current ip address and try to find the
        // ? latest
        // ? record from access_record
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
        if (this.refreshTokenUtil.validateToken(refreshToken)) {
            String username = this.refreshTokenUtil.getUsername(refreshToken);
            Optional<User> user = this.userRepo.findByUserName(username);
            if (user.isPresent() && user.get().isEnabled()) {
                Optional<AccessRecord> latestAccess = this.accessRecordRepo.getLatestAccess(user.get().getUserId(), ip);
                if (latestAccess.isPresent()) {
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
                                latestAccess.get().getRefreshTokenExpiryTime(), new Date(), Action.HEART_BEAT));
                        return new AuthResponse(accessToken, refreshToken);
                    } else {
                        String accessToken = latestAccess.get().getAccessToken();
                        this.accessRecordRepo.save(new AccessRecord(user.get().getUserId(), ip, accessToken,
                                latestAccess.get().getAccessTokenExpiryTime(), latestAccess.get().getRefreshToken(),
                                latestAccess.get().getRefreshTokenExpiryTime(), new Date(), Action.HEART_BEAT));
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
}
