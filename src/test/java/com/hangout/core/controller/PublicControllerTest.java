package com.hangout.core.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.hangout.core.entity.User;
import com.hangout.core.repository.UserRepo;

import jakarta.transaction.Transactional;

@SpringBootTest
public class PublicControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private UserRepo userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void testSignup() {

    }

    private User setupUser() {
        User user = new User("testUser", "test@test.com", passwordEncoder.encode("a1@bcdefgh456"));
        user.setEnabled(true);
        user = this.userRepository.save(user);
        return user;
    }

    private void cleanUpUser() {
        this.userRepository.deleteByUserName("testUser");
    }

    @Test
    @Transactional
    void testLogin() throws Exception {
        setupUser();
        this.mockMvc
                .perform(post("/v1/public/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testUser\",\"password\": \"a1@bcdefgh456\"}"))
                .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andDo(print());
        cleanUpUser();
    }
}
