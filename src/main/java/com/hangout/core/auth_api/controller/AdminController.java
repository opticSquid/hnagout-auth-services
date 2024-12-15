package com.hangout.core.auth_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hangout.core.auth_api.dto.request.NewUser;
import com.hangout.core.auth_api.service.UserDetailsServiceImpl;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/admin")
@Tag(name = "Admin Endpoints")
@Slf4j
public class AdminController {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostMapping("/generate-internal-user")
    public ResponseEntity<String> postMethodName(@RequestBody NewUser newUser) {
        this.userDetailsService.addNewInternalUser(newUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
