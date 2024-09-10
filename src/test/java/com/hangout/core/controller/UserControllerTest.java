package com.hangout.core.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.hangout.core.entity.User;
import com.hangout.core.repository.UserRepo;

@Testcontainers
@SpringBootTest
// @DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ActiveProfiles("test")
public class UserControllerTest {
    private MockMvc mockMvc;
    @Autowired
    private UserRepo userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private WebApplicationContext context;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeEach
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    public User setupUser() {
        User user = new User("testUser", "test@test.com", passwordEncoder.encode("a1@bcdefgh456"));
        user.setEnabled(true);
        user = userRepository.save(user);
        return user;

    }

    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/v1/user").with(user(setupUser()))).andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                // .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(true))
                .andDo(print());
    }
}
