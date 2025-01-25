package com.hangout.core.auth_api.service;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.hangout.core.auth_api.repository.AccessRecordRepo;
import com.hangout.core.auth_api.repository.DeviceRepo;
import com.hangout.core.auth_api.repository.UserRepo;

public class RenewTokenServiceTest {
    @Mock
    private UserRepo userRepo;
    @Mock
    private AccessRecordRepo accessRecordRepo;
    @Mock
    private DeviceRepo deviceRepo;
    @InjectMocks
    private RenewTokenService renewTokenService;

    @Test
    void testRenewToken_passes_throws_JwtNotValidException_on_expired_jwt_tokens() {

    }
}
