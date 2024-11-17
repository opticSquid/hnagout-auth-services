package com.hangout.core.auth_service.dto.request;

import com.hangout.core.auth_service.entity.Roles;

public record PublicUserDetails(String username, Roles role) {

}
