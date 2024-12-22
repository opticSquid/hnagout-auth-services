package com.hangout.core.auth_api.dto.request;

import java.math.BigInteger;

import com.hangout.core.auth_api.entity.Roles;

public record PublicUserDetails(BigInteger userId, Roles role, Boolean trustedDevice) {

}
