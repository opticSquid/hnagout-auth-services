package com.hangout.core.auth_service.config;

import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Bean
    RestClient restClient(RestClientBuilderConfigurer rcbc) {
        return rcbc.configure(RestClient.builder()).build();
    }

}