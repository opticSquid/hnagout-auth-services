package com.hangout.core.auth_service.utils;

import java.time.LocalDateTime;

public interface JwtUtil {
    String generateToken(String username);

    Boolean validateToken(String token);

    LocalDateTime getIssuedAt(String token);

    String getUsername(String token);
}
