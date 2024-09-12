package com.hangout.core.auth_service.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
    @Value("${hangout.jwt.secretKey.access}")
    private String ACCESS_SECRET_KEY;
    @Value("${hangout.jwt.secretKey.refresh}")
    private String REFRESH_SECRET_KEY;

    public String generateAccessToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        SecretKey accessSecretKey = getSigningKey(ACCESS_SECRET_KEY);
        // expiration is 5 minutes
        return createToken(claims, username, 1000 * 60 * 5, accessSecretKey);
    }

    public Boolean validateAccessToken(String token) {
        SecretKey accessSecretKey = getSigningKey(ACCESS_SECRET_KEY);
        // ! also check if user exists from the username in token.
        // ! it may happen that signing key is compromised but database is still intact
        return !extractExpiration(token, accessSecretKey).before(new Date());
    }

    public String extractUserNameFromAccessToken(String token) {
        SecretKey accessSecretKey = getSigningKey(ACCESS_SECRET_KEY);
        return extractAllClaims(token, accessSecretKey).getSubject();
    }

    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        SecretKey refreshSecretKey = getSigningKey(REFRESH_SECRET_KEY);
        // expiration is 7 days
        return createToken(claims, username, 1000 * 60 * 60 * 24 * 7, refreshSecretKey);
    }

    public Boolean validateRefreshToken(String token) {
        SecretKey refreshSecretKey = getSigningKey(REFRESH_SECRET_KEY);
        // ! also check if user exists from the username in token.
        // ! it may happen that signing key is compromised but database is still intact
        return !extractExpiration(token, refreshSecretKey).before(new Date());
    }

    public String extractUserNameFromRefreshToken(String token) {
        SecretKey refreshSecretKey = getSigningKey(REFRESH_SECRET_KEY);
        return extractAllClaims(token, refreshSecretKey).getSubject();
    }

    // ? 'long', not 'Long' is used for compatibility with int because access key
    // ? expiration will fall in range of int
    // ? but refresh key expiration may overflow int boundary
    private String createToken(Map<String, Object> claims, String subject, long expiration, SecretKey singingKey) {
        long currentTimeStamp = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .header().empty().add("typ", "JWT")
                .and()
                .issuedAt(new Date(currentTimeStamp))
                .expiration(new Date(currentTimeStamp + expiration))
                .signWith(singingKey)
                .compact();

    }

    private SecretKey getSigningKey(String key) {
        return Keys.hmacShaKeyFor(key.getBytes());
    }

    private Date extractExpiration(String token, SecretKey singingKey) {
        return extractAllClaims(token, singingKey).getExpiration();
    }

    private Claims extractAllClaims(String token, SecretKey singingKey) {
        return Jwts.parser()
                .verifyWith(singingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
