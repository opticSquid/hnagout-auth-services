package com.hangout.core.auth_api.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hangout.core.auth_api.dto.response.DefaultResponse;
import com.hangout.core.auth_api.entity.AccessRecord;
import com.hangout.core.auth_api.entity.Action;
import com.hangout.core.auth_api.entity.User;
import com.hangout.core.auth_api.exceptions.UnauthorizedAccessException;
import com.hangout.core.auth_api.exceptions.UserNotFoundException;
import com.hangout.core.auth_api.repository.AccessRecordRepo;
import com.hangout.core.auth_api.repository.UserRepo;

import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
class LogoutService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AccessRecordRepo accessRecordRepo;

    @Observed(name = "logout", contextualName = "service")
    public DefaultResponse logout(String userName, String ip) {
        Optional<User> user = this.userRepo.findByUserName(userName);
        if (user.isPresent()) {
            Optional<AccessRecord> access = this.accessRecordRepo.getLatestAccess(user.get().getUserId(), ip);
            if (access.isPresent()) {
                this.accessRecordRepo.save(new AccessRecord(user.get().getUserId(), ip, access.get().getAccessToken(),
                        access.get().getAccessTokenExpiryTime(), access.get().getRefreshToken(),
                        access.get().getRefreshTokenExpiryTime(), new Date(), Action.LOGOUT));
                return new DefaultResponse("Successfully Logged out from this device");
            } else {
                throw new UnauthorizedAccessException("There is no active sesion in the current device");
            }

        } else {
            // this will never occur because if the user did not exist in db they would not
            // have been authenticated in first place
            // thus not allowed to access this route
            // but still keeping it just in case
            throw new UserNotFoundException("Current user is not found in database");
        }
    }
}
