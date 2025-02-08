package com.hangout.core.auth_api.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.hangout.core.auth_api.dto.request.DeviceDetails;
import com.hangout.core.auth_api.dto.response.IpDetails;
import com.hangout.core.auth_api.entity.Device;
import com.hangout.core.auth_api.entity.User;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DeviceUtil {
    private static final Map<String, Integer> WEIGHTS = new HashMap<>();
    private static Integer TOTALWEIGHT;
    private static final Integer THRESHOLD = 15;
    @Autowired
    private RestClient restClient;
    @Value("${hangout.ip-api.url}")
    private String ipApi;
    static {
        WEIGHTS.put("os", 9);
        WEIGHTS.put("screen", 8);
        WEIGHTS.put("userAgent", 7);
        WEIGHTS.put("continent", 10);
        WEIGHTS.put("country", 9);
        WEIGHTS.put("timeZone", 6);
        WEIGHTS.put("regionName", 6);
        WEIGHTS.put("city", 5);
        WEIGHTS.put("isp", 4);
        WEIGHTS.put("asName", 4);
        WEIGHTS.put("mobile", 9);
        WEIGHTS.put("proxy", 9);
        WEIGHTS.put("hosting", 9);
        TOTALWEIGHT = WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Fetches the device details using ip, constructs a new device object but sets
     * it as untrusted device
     * 
     * @param deviceDetails device details coming from request header
     * @param user          current user
     * @return untrusted device object
     */
    public Device buildDeviceProfile(DeviceDetails deviceDetails, User user) {
        String ip = deviceDetails.ip();

        // Check for private/local IPs
        if (deviceDetails.ip().startsWith("0") || deviceDetails.ip().startsWith("127")
                || deviceDetails.ip().startsWith("10")
                || deviceDetails.ip().startsWith("172")
                || deviceDetails.ip().startsWith("192")) {
            return new Device(deviceDetails, createTestIpDetails(), user);
        }

        try {
            ResponseEntity<IpDetails> response = restClient.get()
                    .uri(ipApi + "/json/" + ip
                            + "?fields=status,message,continent,country,timezone,regionName,city,isp,asname,mobile,proxy,hosting&lang=en")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .toEntity(IpDetails.class);

            IpDetails ipDetails = response.getBody();

            if (response.getStatusCode().is4xxClientError() || ipDetails == null
                    || !"success".equals(ipDetails.status())) {
                log.warn("Failed to fetch IP details for {}: {}", ip,
                        ipDetails != null ? ipDetails.message() : "No response body");
                return null;
            }

            return new Device(deviceDetails, ipDetails, user);

        } catch (Exception e) {
            log.error("Error fetching IP details for {}: {}", ip, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Computes a simple fingerprint hash based on weighted properties.
     */
    public Integer computeFingerprint(Device device) {
        return Objects.hash(
                device.getOs().hashCode() * WEIGHTS.get("os"),
                (device.getScreenHeight() + device.getScreenWidth()) * WEIGHTS.get("screen"),
                device.getUserAgent().hashCode() * WEIGHTS.get("userAgent"),
                device.getContinent().hashCode() * WEIGHTS.get("continent"),
                device.getCountry().hashCode() * WEIGHTS.get("country"),
                device.getTimeZone().hashCode() * WEIGHTS.get("timeZone"),
                device.getRegionName().hashCode() * WEIGHTS.get("regionName"),
                device.getCity().hashCode() * WEIGHTS.get("city"),
                device.getIsp().hashCode() * WEIGHTS.get("isp"),
                device.getAsName().hashCode() * WEIGHTS.get("asName"),
                Boolean.hashCode(device.getMobile()) * WEIGHTS.get("mobile"),
                Boolean.hashCode(device.getProxy()) * WEIGHTS.get("proxy"),
                Boolean.hashCode(device.getHosting()) * WEIGHTS.get("hosting"));
    }

    /**
     * Checks if a new login is from a different device.
     */
    public static Boolean isNewDevice(Device oldDevice, Device newDevice) {
        int similarity = compareDevices(oldDevice, newDevice);
        return similarity < THRESHOLD;
    }

    /**
     * Compares two device profiles and returns a similarity score.
     * Higher score = more similarity. If below threshold, treat as a new device.
     */
    private static Integer compareDevices(Device oldDevice, Device newDevice) {
        Integer similarityScore = 0;

        similarityScore += compareProperty(oldDevice.getOs(), newDevice.getOs(), "os");
        similarityScore += compareScreenResolution(oldDevice, newDevice);
        similarityScore += compareProperty(oldDevice.getUserAgent(), newDevice.getUserAgent(), "userAgent");
        similarityScore += compareProperty(oldDevice.getContinent(), newDevice.getContinent(), "continent");
        similarityScore += compareProperty(oldDevice.getCountry(), newDevice.getCountry(), "country");
        similarityScore += compareProperty(oldDevice.getTimeZone(), newDevice.getTimeZone(), "timeZone");
        similarityScore += compareProperty(oldDevice.getRegionName(), newDevice.getRegionName(), "regionName");
        similarityScore += compareProperty(oldDevice.getCity(), newDevice.getCity(), "city");
        similarityScore += compareProperty(oldDevice.getIsp(), newDevice.getIsp(), "isp");
        similarityScore += compareProperty(oldDevice.getAsName(), newDevice.getAsName(), "asName");
        similarityScore += compareProperty(oldDevice.isMobile(), newDevice.isMobile(), "mobile");
        similarityScore += compareProperty(oldDevice.isProxy(), newDevice.isProxy(), "proxy");
        similarityScore += compareProperty(oldDevice.isHosting(), newDevice.isHosting(), "hosting");

        Integer similarityPercentage = (similarityScore * 100) / TOTALWEIGHT;
        log.debug("similarity score: {}", similarityPercentage);
        return similarityPercentage;
    }

    /**
     * Compares two properties and assigns a score based on their weight.
     */
    private static int compareProperty(Object oldVal, Object newVal, String property) {
        return oldVal.equals(newVal) ? WEIGHTS.get(property) : 0;
    }

    // Allow some room for small dpi changes in case of zomm or fractional scaling
    private static int compareScreenResolution(Device oldDevice, Device newDevice) {
        int weight = DeviceUtil.WEIGHTS.get("screen");

        int oldPixels = oldDevice.getScreenHeight() * oldDevice.getScreenWidth();
        int newPixels = newDevice.getScreenHeight() * newDevice.getScreenWidth();

        if (oldPixels == newPixels) {
            return weight;
        }

        // Allow small variations (e.g., DPI scaling or virtual keyboards)
        double ratio = (double) Math.min(oldPixels, newPixels) / Math.max(oldPixels, newPixels);
        if (ratio >= 0.95) { // Allowing 5% difference
            return weight / 2;
        }

        return 0;
    }

    /**
     * Creates a dummy IpDetails object for local/private IPs.
     */
    private IpDetails createTestIpDetails() {
        return new IpDetails("test", Optional.empty(), "test", "test", "test", "test",
                "test", "test", "test", false, false, false);
    }

}
