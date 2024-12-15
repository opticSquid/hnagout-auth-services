package com.hangout.core.auth_api.dto.request;

import com.hangout.core.auth_api.entity.Roles;

public record PublicUserDetails(String username, Roles role, Boolean trustedDevice) {

}
