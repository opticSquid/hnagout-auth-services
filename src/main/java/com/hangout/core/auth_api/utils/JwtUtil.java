package com.hangout.core.auth_api.utils;

import java.util.Date;
import java.util.UUID;

public interface JwtUtil {
    String generateToken(String username, UUID deviceId);

    Boolean validateToken(String token);

    Date getExpiresAt(String token);

    String getUsername(String token);

    UUID getDeviceId(String token);
}
