package com.hangout.core.auth_api.config;

import java.time.Duration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.ResourceAttributes;

@Configuration
public class OpenTelemetryConfig {
        @Value("${hangout.otel-collector.url}")
        private String collectorUrl;

        @Bean
        OpenTelemetry openTelemetry(SdkLoggerProvider sdkLoggerProvider, SdkTracerProvider sdkTracerProvider,
                        ContextPropagators contextPropagators) {
                OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
                                .setLoggerProvider(sdkLoggerProvider)
                                .setTracerProvider(sdkTracerProvider)
                                .setPropagators(contextPropagators)
                                .build();
                OpenTelemetryAppender.install(openTelemetrySdk);
                return openTelemetrySdk;
        }

        @Bean
        SdkLoggerProvider otelSdkLoggerProvider(Environment environment,
                        ObjectProvider<LogRecordProcessor> logRecordProcessors) {
                String applicationName = environment.getProperty("spring.application.name",
                                "application");
                Resource springResource = Resource
                                .create(Attributes.of(ResourceAttributes.SERVICE_NAME, applicationName));
                SdkLoggerProviderBuilder builder = SdkLoggerProvider.builder()
                                .setResource(Resource.getDefault().merge(springResource));
                logRecordProcessors.orderedStream().forEach(builder::addLogRecordProcessor);
                return builder.build();
        }

        @Bean
        LogRecordProcessor otelLogRecordProcessor() {
                return BatchLogRecordProcessor
                                .builder(
                                                OtlpHttpLogRecordExporter.builder()
                                                                .setEndpoint(collectorUrl + "/v1/logs")
                                                                .build())
                                .build();
        }

        @Bean
        SdkTracerProvider sdkTracerProvider(Resource resource, SpanProcessor batchSpanProcessor,
                        SpanExporter otlpHttpSpanExporter, Sampler parentBasedSampler, Sampler traceIdRatioBased,
                        SpanLimits spanLimits) {
                return SdkTracerProvider.builder()
                                .setResource(resource)
                                .addSpanProcessor(
                                                batchSpanProcessor(otlpHttpSpanExporter))
                                .setSampler(parentBasedSampler(traceIdRatioBased))
                                .setSpanLimits(spanLimits)
                                .build();
        }

        @Bean
        SpanProcessor batchSpanProcessor(SpanExporter spanExporter) {
                return BatchSpanProcessor.builder(spanExporter)
                                .setMaxQueueSize(2048)
                                .setExporterTimeout(Duration.ofSeconds(30))
                                .setScheduleDelay(Duration.ofSeconds(5))
                                .build();
        }

        @Bean
        SpanExporter otlpHttpSpanExporter() {
                return OtlpHttpSpanExporter.builder()
                                .setEndpoint(collectorUrl + "/v1/traces")
                                .addHeader("api-key", "value")
                                .setTimeout(Duration.ofSeconds(10))
                                .build();
        }

        @Bean
        Sampler parentBasedSampler(Sampler root) {
                return Sampler.parentBasedBuilder(root)
                                .setLocalParentNotSampled(Sampler.alwaysOff())
                                .setLocalParentSampled(Sampler.alwaysOn())
                                .setRemoteParentNotSampled(Sampler.alwaysOff())
                                .setRemoteParentSampled(Sampler.alwaysOn())
                                .build();
        }

        @Bean
        Sampler traceIdRatioBased() {
                return Sampler.traceIdRatioBased(1);
        }

        @Bean
        SpanLimits spanLimits() {
                return SpanLimits.builder()
                                .setMaxNumberOfAttributes(128)
                                .setMaxAttributeValueLength(1024)
                                .setMaxNumberOfLinks(128)
                                .setMaxNumberOfAttributesPerLink(128)
                                .setMaxNumberOfEvents(128)
                                .setMaxNumberOfAttributesPerEvent(128)
                                .build();
        }
}
