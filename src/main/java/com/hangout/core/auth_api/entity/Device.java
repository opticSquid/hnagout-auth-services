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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deviceId;
    private String os;
    private Integer screenHeight;
    private Integer screenWidth;
    private String userAgent;
    private String continent;
    private String country;
    private String timeZone;
    private String regionName;
    private String city;
    private String isp;
    private String asName;
    private Boolean mobile;
    private Boolean proxy;
    private Boolean hosting;
    private Boolean trusted;
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;
    @OneToMany(mappedBy = "device", fetch = FetchType.LAZY)
    private List<AccessRecord> accessRecords;

    public Device(DeviceDetails deviceDetails, IpDetails ipDetails,
            User user) {
        this.os = deviceDetails.os();
        this.screenWidth = deviceDetails.screenWidth();
        this.screenHeight = deviceDetails.screenHeight();
        this.userAgent = deviceDetails.userAgent();
        this.continent = ipDetails.continent();
        this.country = ipDetails.country();
        this.timeZone = ipDetails.timezone();
        this.regionName = ipDetails.regionName();
        this.city = ipDetails.city();
        this.isp = ipDetails.isp();
        this.asName = ipDetails.asname();
        this.mobile = ipDetails.mobile();
        this.proxy = ipDetails.proxy();
        this.hosting = ipDetails.hosting();
        this.user = user;
        this.trusted = false;
    }

    public void addAccessRecord(AccessRecord accessRecord) {
        if (this.accessRecords == null) {
            this.accessRecords = new ArrayList<>();
        }
        this.accessRecords.add(accessRecord);
    }

    public void trustDevice() {
        this.trusted = true;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public boolean isMobile() {
        return this.mobile;
    }

    public boolean isProxy() {
        return this.proxy;
    }

    public boolean isHosting() {
        return this.hosting;
    }

    public boolean isTrusted() {
        return this.trusted;
    }
}
