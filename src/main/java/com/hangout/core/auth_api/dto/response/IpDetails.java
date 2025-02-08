package com.hangout.core.auth_api.dto.response;

import java.util.Optional;

public record IpDetails(String status,
                Optional<String> message,
                String continent,
                String country,
                String timezone,
                String regionName,
                String city,
                String isp,
                String asname,
                boolean mobile,
                boolean proxy,
                boolean hosting) {

}
