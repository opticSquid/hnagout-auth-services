package com.hangout.core.auth_api.entity;

import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "access_records")
public class AccessRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;
    private BigInteger userId;
    private String ipAddress;
    private String accessToken;
    private ZonedDateTime accessTokenExpiryTime;
    private String refreshToken;
    private ZonedDateTime refreshTokenExpiryTime;
    private ZonedDateTime lastSeen;
    private Action action;

    public AccessRecord(BigInteger userId, String ipAddress, String accessToken, Date accessTokenExpiryTime,
            String refreshToken,
            Date refreshTokenExpiryTime, Date lastSeen, Action action) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.accessToken = accessToken;
        this.accessTokenExpiryTime = accessTokenExpiryTime.toInstant().atZone(ZoneOffset.UTC);
        this.refreshToken = refreshToken;
        this.refreshTokenExpiryTime = refreshTokenExpiryTime.toInstant().atZone(ZoneOffset.UTC);
        this.lastSeen = lastSeen.toInstant().atZone(ZoneOffset.UTC);
        this.action = action;
    }

    public AccessRecord(BigInteger userId, String ipAddress, String accessToken, ZonedDateTime accessTokenExpiryTime,
            String refreshToken, ZonedDateTime refreshTokenExpiryTime, Date lastSeen, Action action) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.accessToken = accessToken;
        this.accessTokenExpiryTime = accessTokenExpiryTime;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiryTime = refreshTokenExpiryTime;
        this.lastSeen = lastSeen.toInstant().atZone(ZoneOffset.UTC);
        this.action = action;
    }

}
