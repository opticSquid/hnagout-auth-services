package com.hangout.core.auth_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;

@Configuration
public class OtelConfiguration {
    @Value("${hangout.tempo-server.url}")
    private String tempoUrl;

    @Bean
    SpanExporter spanExporter() {
        return OtlpGrpcSpanExporter.builder().setEndpoint(tempoUrl).build();
    }
}
