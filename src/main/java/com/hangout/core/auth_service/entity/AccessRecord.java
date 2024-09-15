package com.hangout.core.auth_service.entity;

import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class AccessRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private BigInteger id;
    private BigInteger userId;
    private String ipAddress;
    private String accessToken;
    private ZonedDateTime accessTokenIssueTime;
    private String refreshToken;
    private ZonedDateTime refresTokenIssueTime;
    private ZonedDateTime lastSeen;
    private Action action;

    public AccessRecord(BigInteger userId, String ipAddress, String accessToken, Date accessTokenIssueTime,
            String refreshToken,
            Date refresTokenIssueTime, Date lastSeen, Action action) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.accessToken = accessToken;
        this.accessTokenIssueTime = accessTokenIssueTime.toInstant().atZone(ZoneOffset.UTC);
        this.refreshToken = refreshToken;
        this.refresTokenIssueTime = refresTokenIssueTime.toInstant().atZone(ZoneOffset.UTC);
        this.lastSeen = lastSeen.toInstant().atZone(ZoneOffset.UTC);
        this.action = action;
    }

    public AccessRecord(BigInteger userId, String ipAddress, String accessToken, ZonedDateTime accessTokenIssueTime,
            String refreshToken, ZonedDateTime refresTokenIssueTime, Date lastSeen, Action action) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.accessToken = accessToken;
        this.accessTokenIssueTime = accessTokenIssueTime;
        this.refreshToken = refreshToken;
        this.refresTokenIssueTime = refresTokenIssueTime;
        this.lastSeen = lastSeen.toInstant().atZone(ZoneOffset.UTC);
        this.action = action;
    }

}
