package com.hangout.core.auth_api.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.hangout.core.auth_api.entity.Device;
import com.hangout.core.auth_api.entity.User;
import com.hangout.core.auth_api.dto.request.DeviceDetails;
import lombok.extern.slf4j.Slf4j;
import com.hangout.core.auth_api.dto.response.IpDetails;

@Component
@Slf4j
public class DeviceUtil {
    private static Map<String, Double> weights = new HashMap<>();
    private static Integer maxScore = 70;
    @Autowired
    private RestClient restClient;
    @Value("${hangout.ip-api.url}")
    private String ipApi;
    static {
        weights.put("screenWidth", 10.0);
        weights.put("screenHeight", 10.0);
        weights.put("os", 10.0);
        weights.put("userAgent", 10.0);
        weights.put("country", 9.0);
        weights.put("timeZone", 8.0);
        weights.put("region", 7.0);
        weights.put("isp", 4.0);
        weights.put("ip", 2.0);

    }

    public Double calculateDeviceSimilarity(Device d1, Device d2) {
        Double totalScore = 0.0;
        if (d1.getScreenHeight() == d2.getScreenHeight()) {
            totalScore += weights.get("screenHeight");
        }
        if (d1.getScreenWidth() == d2.getScreenWidth()) {
            totalScore += weights.get("screenWidth");
        }
        if (d1.getOs().equals(d2.getOs())) {
            totalScore += weights.get("os");
        }
        if (d1.getCountry().equals(d2.getCountry())) {
            totalScore += weights.get("country");
        }
        if (d1.getUserAgent().equals(d2.getUserAgent())) {
            totalScore += weights.get("userAgent");
        }
        if (d1.getTimeZone().equals(d2.getTimeZone())) {
            totalScore += weights.get("timeZone");
        }
        if (d1.getRegion().equals(d2.getRegion())) {
            totalScore += weights.get("region");
        }
        if (d1.getIsp().equals(d2.getIsp())) {
            totalScore += weights.get("isp");
        }
        if (d1.getIp().equals(d2.getIp())) {
            totalScore += weights.get("ip");
        }
        log.debug("total score: {}", totalScore);
        return (totalScore / maxScore) * 100.00;
    }

    /**
     * Fetches the device details using ip, constructs a new device object but sets
     * it as untrusted device
     * 
     * @param deviceDetails device details coming from request header
     * @param user          current user
     * @return untrusted device object
     */
    @SuppressWarnings("null")
    public Device getDevice(DeviceDetails deviceDetails, User user) {
        if (deviceDetails.ip().startsWith("0") || deviceDetails.ip().startsWith("172")
                || deviceDetails.ip().startsWith("192")) {
            return new Device(deviceDetails,
                    new IpDetails("success", "India", "IN", "WB", "West Bengal", "Kolkata", 700156, 22.51, 82.685,
                            "Asia/Kolkata", "Reliance Jio", "Reliance Jio Infocomm Pvt Ltd",
                            "AS55836 Reliance Jio Infocomm Limited", deviceDetails.ip()),
                    user, false);
        }
        ResponseEntity<IpDetails> ipDetails = this.restClient
                .get()
                .uri(ipApi + "/json/" + deviceDetails.ip())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(IpDetails.class);
        if (ipDetails.getStatusCode().is4xxClientError() || !ipDetails.getBody().status().equals("success")) {
            return null;
        }
        return new Device(deviceDetails, ipDetails.getBody(), user, false);
    }
}
