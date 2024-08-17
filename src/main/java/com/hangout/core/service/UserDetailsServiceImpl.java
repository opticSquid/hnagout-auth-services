package com.hangout.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hangout.core.dto.NewUser;
import com.hangout.core.entity.User;
import com.hangout.core.repository.UserRepo;

import jakarta.transaction.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUserName(username);
        if (user != null) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
        }
        throw new UsernameNotFoundException("User not found with username: " + username);
    }

    public void addNewUser(NewUser user) throws Exception {
        User newUser = new User(user.username(), user.email(), passwordEncoder.encode(user.password()));
        newUser.setEnabled(true);
        userRepo.save(newUser);
    }

    @Transactional
    public void deleteUser(String username) throws Exception {
        this.userRepo.deleteByUserName(username);
    }
}
