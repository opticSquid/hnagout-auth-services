package com.hangout.core.auth_api.dto.response;

public record IpDetails(String status, String country, String countryCode, String region, String regionName,
                String city, Integer zip, Double lat, Double lon, String timeZone, String isp, String org, String as,
                String query) {

}
