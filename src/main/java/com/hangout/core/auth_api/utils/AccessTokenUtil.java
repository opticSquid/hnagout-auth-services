package com.hangout.core.auth_api.utils;

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
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AccessTokenUtil implements JwtUtil {
    @Value("${hangout.jwt.secretKey.access}")
    private String ACCESS_SECRET_KEY;

    @Override
    @Observed(name = "generate-token", contextualName = "access-token")
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        // expiration is 5 minutes
        return createToken(username, claims, 1000 * 60 * 5);
    }

    @Override
    @Observed(name = "validate-token", contextualName = "access-token")
    public Boolean validateToken(String token) {
        log.info("Validating access token");
        if (!token.isEmpty()) {
            Date expirationTime = this.extractAllClaims(token).getExpiration();
            // check if the expirtation is in past of current instant
            return !expirationTime.before(new Date());
        } else {
            return false;
        }
    }

    @Override
    @Observed(name = "get-expires-at", contextualName = "access-token")
    public Date getExpiresAt(String token) {
        Date issueTime = this.extractAllClaims(token).getExpiration();
        return issueTime;
    }

    @Override
    @Observed(name = "get-username", contextualName = "access-token")
    public String getUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(ACCESS_SECRET_KEY.getBytes());
    }

    // ? 'long', not 'Long' is used for compatibility with int because access key
    // ? expiration will fall in range of int
    // ? but refresh key expiration may overflow int boundary
    private String createToken(String subject, Map<String, Object> claims, long expiration) {
        long currentTimeStamp = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .header().empty().add("typ", "JWT")
                .and()
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
