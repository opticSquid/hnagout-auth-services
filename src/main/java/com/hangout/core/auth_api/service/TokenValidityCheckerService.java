package com.hangout.core.auth_api.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hangout.core.auth_api.dto.request.PublicUserDetails;
import com.hangout.core.auth_api.entity.AccessRecord;
import com.hangout.core.auth_api.entity.Action;
import com.hangout.core.auth_api.entity.User;
import com.hangout.core.auth_api.exceptions.UnauthorizedAccessException;
import com.hangout.core.auth_api.exceptions.UserNotFoundException;
import com.hangout.core.auth_api.repository.AccessRecordRepo;
import com.hangout.core.auth_api.repository.UserRepo;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
class TokenValidityCheckerService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private AccessRecordRepo accessRecordRepo;

    public PublicUserDetails checkTokenValidity(String username, String ip) {
        // get user from database
        log.debug("Username: {}, ip: {}", username, ip);
        // ! for dev only
        ip = ip.equals("127.0.0.1") ? "0:0:0:0:0:0:0:1" : ip;
        Optional<User> user = this.userRepo.findByUserName(username);
        if (user.isPresent() && user.get().isEnabled()) {
            Optional<AccessRecord> latestAccess = this.accessRecordRepo.getLatestAccessRecord(user.get().getUserId(),
                    ip);
            // check if the last action of the user in the given device was not logout
            if (latestAccess.isPresent() && !latestAccess.get().getUserAction().equals(Action.LOGOUT)) {
                return new PublicUserDetails(username, user.get().getRole());
            } else {
                throw new UnauthorizedAccessException("User is not authorized to access this route");
            }
        } else {
            throw new UserNotFoundException("User indicated by the token was not found");
        }
    }
}
