package com.hangout.core.auth_api.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.hangout.core.auth_api.dto.request.DeviceDetails;
import com.hangout.core.auth_api.dto.response.IpDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "devices")
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
    private Boolean isTrusted;
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;
    @OneToMany(mappedBy = "device", fetch = FetchType.LAZY)
    private List<AccessRecord> accessRecords;

    public Device(DeviceDetails deviceDetails, IpDetails ipDetails,
            User user, Boolean trusted) {
        this.os = deviceDetails.os();
        this.screenWidth = deviceDetails.screenWidth();
        this.screenHeight = deviceDetails.screenHeight();
        this.userAgent = deviceDetails.userAgent();
        this.country = ipDetails.country();
        this.region = ipDetails.region();
        this.timeZone = ipDetails.timeZone();
        this.isp = ipDetails.isp();
        this.ip = ipDetails.query();
        this.user = user;
        this.isTrusted = trusted;
    }

    public void addAccessRecord(AccessRecord accessRecord) {
        if (this.accessRecords == null) {
            this.accessRecords = new ArrayList<>();
        }
        this.accessRecords.add(accessRecord);
    }

    public void trustDevice() {
        this.isTrusted = true;
    }
}
