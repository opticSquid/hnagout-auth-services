package com.hangout.core.hangoutauthservice.config;

import com.hangout.core.hangoutauthservice.dto.NewVerifiedUserEvent;
import com.hangout.core.hangoutauthservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageConsumer {
    private final AuthenticationService authService;
    @KafkaListener(topics = "new-verified-user", groupId = "my-group-id")
    public void listen(NewVerifiedUserEvent event) {
            authService.userVerified(event);
    }

}
