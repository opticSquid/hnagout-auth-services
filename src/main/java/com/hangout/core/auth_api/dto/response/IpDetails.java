package com.hangout.core.auth_api.dto.response;

public record IpDetails(String query, String status, String country,
                String countryCode, String region, String regionName,
                String city, String zip, Double lat, Double lon, String timezone, String isp, String org,
                String as) {

}
