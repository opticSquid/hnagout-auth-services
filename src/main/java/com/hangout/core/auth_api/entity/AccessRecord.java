package com.hangout.core.auth_api.entity;

import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    private BigInteger recordId;
    @Column(length = 512)
    private String accessToken;
    private ZonedDateTime accessTokenExpiryTime;
    @Column(length = 512)
    private String refreshToken;
    private ZonedDateTime refreshTokenExpiryTime;
    private Action userAction;
    private ZonedDateTime recordCreatedAt;
    @ManyToOne
    @JoinColumn(name = "deviceId", nullable = false)
    private Device device;
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    public AccessRecord(String accessToken, Date accessTokenExpiryTime,
            String refreshToken,
            Date refreshTokenExpiryTime, Action action, Device device, User user) {
        this.accessToken = accessToken;
        this.accessTokenExpiryTime = accessTokenExpiryTime.toInstant().atZone(ZoneOffset.UTC);
        this.refreshToken = refreshToken;
        this.refreshTokenExpiryTime = refreshTokenExpiryTime.toInstant().atZone(ZoneOffset.UTC);
        this.userAction = action;
        this.recordCreatedAt = ZonedDateTime.now(ZoneOffset.UTC);
        this.device = device;
        this.user = user;
    }

    public AccessRecord(String accessToken, ZonedDateTime accessTokenExpiryTime,
            String refreshToken, ZonedDateTime refreshTokenExpiryTime, Action action, Device device, User user) {
        this.accessToken = accessToken;
        this.accessTokenExpiryTime = accessTokenExpiryTime;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiryTime = refreshTokenExpiryTime;
        this.userAction = action;
        this.recordCreatedAt = ZonedDateTime.now(ZoneOffset.UTC);
        this.device = device;
        this.user = user;
    }

}
