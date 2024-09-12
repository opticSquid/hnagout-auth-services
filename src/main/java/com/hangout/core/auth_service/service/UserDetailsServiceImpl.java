package com.hangout.core.auth_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hangout.core.auth_service.dto.request.NewUser;
import com.hangout.core.auth_service.entity.User;
import com.hangout.core.auth_service.repository.UserRepo;

import jakarta.transaction.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    KafkaTemplate<String, String> eventBus;
    @Value("${hangout.kafka.topic.verification-mail}")
    private String verificationEmailTopic;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUserName(username);
        if (user != null) {
            return org.springframework.security.core.userdetails.User.builder().username(user.getUsername())
                    .password(user.getPassword()).roles(user.getRole().name()).build();
        }
        throw new UsernameNotFoundException("User not found with username: " + username);
    }

    public void addNewUser(NewUser user) throws Exception {
        User newUser = new User(user.username(), user.email(), passwordEncoder.encode(user.password()));
        userRepo.save(newUser);
        // produce an event in kafka to send verification email
        eventBus.send(verificationEmailTopic, "email", user.email());
    }

    @Transactional
    public void deleteUser(String username) throws Exception {
        this.userRepo.deleteByUserName(username);
    }
}
