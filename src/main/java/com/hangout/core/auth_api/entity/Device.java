package com.hangout.core.auth_api.entity;

import java.util.List;
import java.util.UUID;

import com.hangout.core.auth_api.dto.response.DeviceDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deviceId;
    private String os;
    private Integer screenWidth;
    private Integer screenHeight;
    private String userAgent;
    private String country;
    private String region;
    private String timeZone;
    private String isp;
    private String ip;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OneToMany(mappedBy = "device")
    private List<AccessRecord> accessRecords;

    public Device(String os, Integer screenWidth, Integer screenHeight, String userAgent, DeviceDetails deviceDetails,
            User user) {
        this.os = os;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.userAgent = userAgent;
        this.country = deviceDetails.country();
        this.region = deviceDetails.region();
        this.timeZone = deviceDetails.timeZone();
        this.isp = deviceDetails.isp();
        this.ip = deviceDetails.query();
        this.user = user;
    }

}
