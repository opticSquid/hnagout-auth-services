package com.hangout.core.auth_service.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.hangout.core.auth_service.dto.event.AccountActivationMailEvent;
import com.hangout.core.auth_service.dto.event.VerifyAccountEvent;
import com.hangout.core.auth_service.dto.request.NewUser;
import com.hangout.core.auth_service.dto.response.AccountVerficationResponse;
import com.hangout.core.auth_service.entity.User;
import com.hangout.core.auth_service.exceptions.JwtNotValidException;
import com.hangout.core.auth_service.exceptions.UserNotFoundException;
import com.hangout.core.auth_service.repository.UserRepo;

import jakarta.transaction.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private KafkaTemplate<String, Object> eventBus;
    @Value("${hangout.kafka.topic.verification-mail}")
    private String verificationEmailTopic;
    private final RestClient restClient = RestClient.create();
    @Value("${hangout.notification-service.url}")
    private String notificationService;
    @Value("${hangout.kafka.topic.activation-mail}")
    private String activationEmailTopic;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByUserName(username);
        if (user.isPresent()) {
            return org.springframework.security.core.userdetails.User.builder().username(user.get().getUsername())
                    .password(user.get().getPassword()).roles(user.get().getRole().name()).build();
        }
        throw new UsernameNotFoundException("User not found with username: " + username);
    }

    public void addNewUser(NewUser user) throws Exception {
        User newUser = new User(user.username(), user.email(), passwordEncoder.encode(user.password()));
        userRepo.save(newUser);
        // produce an event in kafka to send verification email
        eventBus.send(verificationEmailTopic, new VerifyAccountEvent(newUser.getUsername(), newUser.getEmail()));
    }

    public String verifyToken(String token) {
        try {
            AccountVerficationResponse res = restClient.get().uri(notificationService + "/verify-token").retrieve()
                    .body(AccountVerficationResponse.class);
            if (res != null && res.isVerified()) {
                this.userRepo.activateAccount(res.email());
                Optional<User> user = this.userRepo.findByEmail(res.email());
                // produce an event in kafka to send account activation email
                eventBus.send(activationEmailTopic,
                        new AccountActivationMailEvent(user.get().getUsername(), user.get().getEmail(), 200));
                return "account verified";
            } else {
                throw new JwtNotValidException("verification url is not valid");
            }
        } catch (Exception ex) {
            throw new UserNotFoundException("Account activation failed");
        }

    }

    @Transactional
    public void deleteUser(String username) throws Exception {
        this.userRepo.deleteByUserName(username);
    }
}
