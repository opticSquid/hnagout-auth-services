package com.hangout.core.auth_api.dto.response;

public record DeviceDetails(String status, String country, String countryCode, String region, String regionName,
        String city, Integer zip, Float lat, Float lon, String timeZone, String isp, String org, String as,
        String query) {

}
