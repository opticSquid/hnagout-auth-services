package com.hangout.core.hangoutauthservice.config;

import com.hangout.core.hangoutauthservice.dto.NewUnverifiedUserEvent;
import com.hangout.core.hangoutauthservice.dto.VerificationStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
