package com.hangout.core.hangoutauthservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hangout.core.hangoutauthservice.dto.NewVerifiedUserEvent;
import com.hangout.core.hangoutauthservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageConsumer {
    private final AuthenticationService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // groupId is of the consumer-group that is producing this event
    @KafkaListener(topics = "new-verified-user")
    public void listen(String event) {
        try {
            NewVerifiedUserEvent newVerifiedUserEvent = objectMapper.readValue(event, NewVerifiedUserEvent.class);
            authService.userVerified(newVerifiedUserEvent);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("JSON string could not be converted to POJO");
        }

    }

}
