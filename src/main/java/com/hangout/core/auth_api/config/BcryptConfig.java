package com.hangout.core.auth_api.config;

import java.security.SecureRandom;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BcryptConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        SecureRandom random = new SecureRandom();
        return new BCryptPasswordEncoder(BCryptVersion.$2B, 11, new SecureRandom(random.generateSeed(20)));
    }
}
