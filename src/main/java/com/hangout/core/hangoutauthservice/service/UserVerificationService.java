package com.hangout.core.hangoutauthservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hangout.core.hangoutauthservice.config.MessageProducer;
import com.hangout.core.hangoutauthservice.dto.CheckIntegrityOfToken;
import com.hangout.core.hangoutauthservice.exceptions.DTOConversionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserVerificationService {
    private final MessageProducer messageProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void verifyUser(String token) {
        log.debug("token: {}",token);
        try {
            messageProducer.sendMessage("check-integrity-token", objectMapper.writeValueAsString(new CheckIntegrityOfToken(token)));
        } catch (JsonProcessingException ex) {
            throw new DTOConversionException("exception during conversion of jwt token in email verification to JSON", CheckIntegrityOfToken.class.getSimpleName());
        }

    }

}
