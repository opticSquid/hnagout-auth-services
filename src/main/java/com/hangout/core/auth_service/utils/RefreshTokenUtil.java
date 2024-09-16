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
import io.micrometer.observation.annotation.Observed;

@Component
public class RefreshTokenUtil implements JwtUtil {
    @Value("${hangout.jwt.secretKey.refresh}")
    private String REFRESH_SECRET_KEY;

    @Override
    @Observed(name = "generate-token", contextualName = "refresh-token")
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        // expiration is 7 days
        return createToken(username, claims, 1000 * 60 * 60 * 24 * 7);
    }

    @Override
    @Observed(name = "validate-token", contextualName = "refresh-token")
    public Boolean validateToken(String token) {
        Date expirationTime = this.extractAllClaims(token).getExpiration();
        return !expirationTime.before(new Date());
    }

    @Override
    @Observed(name = "get-expires-at", contextualName = "refresh-token")
    public Date getExpiresAt(String token) {
        Date issueTime = this.extractAllClaims(token).getExpiration();
        return issueTime;
    }

    @Override
    @Observed(name = "get-username", contextualName = "refresh-token")
    public String getUsername(String token) {
        return this.extractAllClaims(token).getSubject();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(REFRESH_SECRET_KEY.getBytes());
    }

    // ? 'long', not 'Long' is used for compatibility with int because access key
    // ? expiration will fall in range of int
    // ? but refresh key expiration may overflow int boundary
    private String createToken(String subject, Map<String, Object> claims, long expiration) {
        long currentTimeStamp = System.currentTimeMillis();
        return Jwts.builder()
                .header().empty().add("typ", "ACS_JWT")
                .and()
                .subject(subject)
                .claims(claims)
                .issuedAt(new Date(currentTimeStamp))
                .expiration(new Date(currentTimeStamp + expiration))
                .signWith(this.getSigningKey())
                .compact();

    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
