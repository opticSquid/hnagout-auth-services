package com.hangout.core.auth_service.service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

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
import com.hangout.core.auth_service.repository.AccessRecordRepo;
import com.hangout.core.auth_service.repository.UserRepo;
import com.hangout.core.auth_service.utils.JwtUtil;

@Service
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
        Date iatAccessToken = this.accessTokenUtil.getIssuedAt(accessJwt);
        String refreshJwt = this.refreshTokenUtil.generateToken(username);
        Date iatRefreshToken = this.refreshTokenUtil.getIssuedAt(refreshJwt);
        // ? we are not checking if user exists or not here because earlier on we have
        // ? already checked that in authentication
        BigInteger userId = this.userRepo.findByUserName(username).get().getUserId();
        // saving login attempt as new record of access
        this.accessRecordRepo.save(
                new AccessRecord(userId, ip, accessJwt, iatAccessToken, refreshJwt, iatRefreshToken, new Date(),
                        Action.LOGIN));
        return new AuthResponse(accessJwt, refreshJwt);
    }
}
