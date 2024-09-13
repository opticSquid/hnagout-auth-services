package com.hangout.core.auth_service.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.hangout.core.auth_service.dto.event.AccountActivationMailEvent;
import com.hangout.core.auth_service.dto.event.VerifyAccountEvent;
import com.hangout.core.auth_service.dto.request.NewUser;
import com.hangout.core.auth_service.dto.request.TokenVerificationRequest;
import com.hangout.core.auth_service.dto.response.AccountVerficationResponse;
import com.hangout.core.auth_service.entity.User;
import com.hangout.core.auth_service.exceptions.JwtNotValidException;
import com.hangout.core.auth_service.exceptions.UserNotFoundException;
import com.hangout.core.auth_service.repository.UserRepo;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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

    @Transactional
    public String verifyToken(String token) {
        try {
            ResponseEntity<AccountVerficationResponse> res = restClient
                    .post()
                    .uri(notificationService + "/verify-token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new TokenVerificationRequest(token))
                    .retrieve()
                    .toEntity(AccountVerficationResponse.class);
            log.debug("response recieved from notification service: {}", res);
            if (res != null && res.getBody().isVerified()) {
                log.debug("Token is verified: {}", res.getBody());
                log.debug("activating account");
                this.userRepo.activateAccount(res.getBody().email());
                Optional<User> user = this.userRepo.findByEmail(res.getBody().email());
                // produce an event in kafka to send account activation email
                log.debug("sending account activation mail");
                eventBus.send(activationEmailTopic,
                        new AccountActivationMailEvent(user.get().getUsername(), user.get().getEmail(), 200));
                return "account verified";
            } else {
                throw new JwtNotValidException("verification url is not valid");
            }
        } catch (RestClientResponseException ex) {
            throw new JwtNotValidException("verification url is not valid");
        } catch (Exception ex) {
            throw new UserNotFoundException("Account activation failed");
        }

    }

    @Transactional
    public void deleteUser(String username) throws Exception {
        this.userRepo.deleteByUserName(username);
    }
}
