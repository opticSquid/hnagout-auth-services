package com.hangout.core.auth_service.entity;

import java.math.BigInteger;
import java.time.LocalDateTime;

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
    private LocalDateTime accessTokenIssueTime;
    private String refreshToken;
    private LocalDateTime refresTokenIssueTime;
    private LocalDateTime lastSeen;
    private Action action;

    public AccessRecord(BigInteger userId, String ipAddress, String accessToken, LocalDateTime accessTokenIssueTime,
            String refreshToken,
            LocalDateTime refresTokenIssueTime, LocalDateTime lastSeen, Action action) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.accessToken = accessToken;
        this.accessTokenIssueTime = accessTokenIssueTime;
        this.refreshToken = refreshToken;
        this.refresTokenIssueTime = refresTokenIssueTime;
        this.lastSeen = lastSeen;
        this.action = action;
    }

}
