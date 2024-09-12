package com.hangout.core.auth_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.hangout.core.auth_service.dto.request.ExistingUser;
import com.hangout.core.auth_service.dto.response.AuthResponse;
import com.hangout.core.auth_service.utils.JwtUtils;

@Service
public class AccessService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;

    public AuthResponse login(ExistingUser user) throws Exception {
        String username = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.username(), user.password())).getName();
        String accessJwt = jwtUtils.generateAccessToken(username);
        String refreshJwt = jwtUtils.generateRefreshToken(username);
        return new AuthResponse(accessJwt, refreshJwt);
    }
}
