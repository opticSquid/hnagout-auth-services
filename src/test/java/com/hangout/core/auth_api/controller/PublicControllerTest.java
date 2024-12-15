package com.hangout.core.auth_api.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.Charset;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.hangout.core.auth_api.dto.request.DeviceDetails;
import com.hangout.core.auth_api.entity.AccessRecord;
import com.hangout.core.auth_api.entity.Action;
import com.hangout.core.auth_api.entity.Device;
import com.hangout.core.auth_api.entity.User;
import com.hangout.core.auth_api.repository.AccessRecordRepo;
import com.hangout.core.auth_api.repository.DeviceRepo;
import com.hangout.core.auth_api.repository.UserRepo;
import com.hangout.core.auth_api.utils.DeviceUtil;
import com.hangout.core.auth_api.utils.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class PublicControllerTest {
    private MockMvc mockMvc;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    @Qualifier("accessTokenUtil")
    private JwtUtil accessTokenUtil;
    @Autowired
    @Qualifier("refreshTokenUtil")
    private JwtUtil refreshTokenUtil;
    @Autowired
    private DeviceUtil deviceUtil;
    @Autowired
    private UserRepo userRepository;
    @Autowired
    private AccessRecordRepo accessRepo;
    @Autowired
    private DeviceRepo deviceRepo;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    @Container
    private static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.8.0")).withKraft()
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @AfterAll
    static void destruct() {
        kafka.close();
    }

    @BeforeEach
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @Transactional
    void testSignup_validUserCredentials() {
        log.info("Starting test signup");
        String userName = "test";
        String email = "test@test.com";
        String password = "test1234567890";
        try {
            this.mockMvc
                    .perform(post("/v1/public/signup").contentType(MediaType.APPLICATION_JSON).content(
                            "{\"username\": \"" + userName
                                    + "\" ,\"email\": \"" + email + "\" , \"password\": \"" + password + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Verification mail sent"))
                    .andDo(print());
        } catch (Exception ex) {
            log.error("Exception occoured", ex);
        } finally {
            cleanUpUser(userName);
        }
    }

    @Test
    @Transactional
    void testLogin_usernamePasswordCorrect_notLoggedInBefore() throws Exception {
        log.info("starting test login correct username password");
        String userName = "testUser";
        String email = "test@test.com";
        String password = "a1@bcdefgh456";
        setupUser(userName, email, password);
        try {
            this.mockMvc
                    .perform(post("/v1/public/login").contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(Charset.defaultCharset())
                            .content("{\"username\": \"" + userName + "\", \"password\":\"" + password + "\" }"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").isString())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isString())
                    .andDo(print());
        } finally {
            cleanUpUser(userName);
        }
    }

    @Test
    @Transactional
    void testLogin_usernamePasswordWrong_notLoggedInBefore() throws Exception {
        log.info("starting test login wrong username password");
        String userName = "testUser";
        String email = "test@test.com";
        String password = "a1@bcdefgh456";
        setupUser(userName, email, password);
        try {
            this.mockMvc
                    .perform(post("/v1/public/login").contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding(Charset.defaultCharset())
                            .content("{\"username\": \"testUser3\",\"password\": \"a1@bcdefgh4567\"}"))
                    .andExpect(status().is4xxClientError())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Username or password is wrong"))
                    .andDo(print());
        } finally {
            cleanUpUser(userName);
        }
    }

    @Test
    @Transactional
    void testRenewToken_validRefToken_accTokenExpired() throws Exception {
        log.info("starting test renew token");
        String userName = "testUser";
        String email = "test@test.com";
        String password = "a1@bcdefgh456";
        User user = setupUser(userName, email, password);
        Device device = this.deviceUtil
                .getDevice(new DeviceDetails("127.0.0.1", "ubuntu/linux", 1920, 1080, "hangout/test"), user);
        device = this.deviceRepo.save(device);
        String accToken = this.accessTokenUtil.generateToken(userName, device.getDeviceId());
        ZonedDateTime accTokenExpiry = this.accessTokenUtil.getExpiresAt(accToken).toInstant().atZone(ZoneOffset.UTC);
        String refToken = this.refreshTokenUtil.generateToken(userName, device.getDeviceId());
        ZonedDateTime refTokenExpiry = this.refreshTokenUtil.getExpiresAt(refToken).toInstant().atZone(ZoneOffset.UTC);
        addLoginRecord(accToken, accTokenExpiry.plusSeconds(30), refToken, refTokenExpiry, device, user);
        try {
            this.mockMvc.perform(post("/v1/public/renew").contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding(Charset.defaultCharset())
                    .content("{ \"token\": \"" + refToken + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value(accToken))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").value(refToken))
                    .andDo(print());
        } finally {
            deleteAccesRecords();
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
    private User setupUser(String userName, String email, String password) {
        User user = new User(userName, email, passwordEncoder.encode(password));
        user.setEnabled(true);
        user = this.userRepository.save(user);
        log.debug("[TEST] user record added to db: {}", user);
        return user;
    }

    private AccessRecord addLoginRecord(String accToken,
            ZonedDateTime accTokenExpiry,
            String refToken, ZonedDateTime refTokenExpiry, Device device, User user) {
        AccessRecord record = this.accessRepo.save(new AccessRecord(accToken,
                accTokenExpiry, refToken,
                refTokenExpiry, Action.LOGIN, device, user));
        log.debug("[TEST] login record added to db: {}", record);
        return record;
    }

    /**
     * given a username deletes the user from database
     * 
     * @param username
     */
    private void cleanUpUser(String username) {
        this.userRepository.deleteByUserName(username);
    }

    private void deleteAccesRecords() {
        this.accessRepo.deleteAll();
    }

}
