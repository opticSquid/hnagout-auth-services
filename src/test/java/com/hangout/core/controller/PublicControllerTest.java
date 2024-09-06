package com.hangout.core.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import com.hangout.core.entity.User;
import com.hangout.core.repository.UserRepo;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class PublicControllerTest {
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

    @Test
    @Transactional
    void testSignup_validUserCredentials() {
        String userName = "test";
        String email = "test@test.com";
        String password = "test1234567890";
        try {
            this.mockMvc
                    .perform(post("/v1/public/signup").contentType(MediaType.APPLICATION_JSON).content(
                            "{\"username\": \"" + userName
                                    + "\" ,\"email\": \"" + email + "\" , \"password\": \"" + password + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/plain;charset=UTF-8"))
                    .andExpect(MockMvcResultMatchers.content().string("Verification mail sent"))
                    .andDo(print());
        } catch (Exception ex) {
            log.error("Exception occoured", ex);
        } finally {
            cleanUpUser(userName);
        }
    }

    @Test
    @Transactional
    void testLogin_usernamePasswordCorrect() throws Exception {
        String userName = "testUser";
        String email = "test@test.com";
        String password = "a1@bcdefgh456";
        setupUser(userName, email, password);
        try {
            this.mockMvc
                    .perform(post("/v1/public/login").contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(Charset.defaultCharset())
                            .content("{\"username\": \"" + userName + "\", \"password\":\"" + password + "\" }"))
                    .andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8"))
                    .andDo(print());
        } finally {
            cleanUpUser(userName);
        }
    }

    @Test
    @Transactional
    void testLogin_usernamePasswordWrong() {
        String userName = "testUser";
        String email = "test@test.com";
        String password = "a1@bcdefgh456";
        setupUser(userName, email, password);
        try {
            this.mockMvc
                    .perform(post("/v1/public/login").contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(Charset.defaultCharset())
                            .content("{\"username\": \"testUser3\",\"password\": \"a1@bcdefgh4567\"}"))
                    .andExpect(status().is4xxClientError()).andExpect(content().contentType("text/plain;charset=UTF-8"))
                    .andDo(print());
        } catch (Exception ex) {
            log.error("Exception occoured during execution of testLogin_usernamePasswordWrong", ex.getMessage());
        } finally {
            cleanUpUser(userName);
        }
    }

    /**
     * sets up a mock user given username, email and password
     * 
     * @param userName
     * @param email
     * @param password
     */
    private void setupUser(String userName, String email, String password) {
        User user = new User(userName, email, passwordEncoder.encode(password));
        user.setEnabled(true);
        user = this.userRepository.save(user);
    }

    /**
     * given a username deletes the user from database
     * 
     * @param username
     */
    private void cleanUpUser(String username) {
        this.userRepository.deleteByUserName(username);
    }

}
