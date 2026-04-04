package com.ai_emailclassifier.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.time.Duration;

/*
 * WHY a config class for RestTemplate?
 * RestTemplate needs timeout configuration — without it, a hung AI API call will block your thread FOREVER.
 * Always set timeouts on external HTTP clients.
 */
@Configuration
public class AppConfig {

    @Value("${ai.openrouter.timeout-seconds}")
    private int timeoutSeconds;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                //Fail fast if server is unreachable
                .connectTimeout(Duration.ofSeconds(10))
                //also will fail if AI takes too long to respond
                .readTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

//    @Bean
//    public ObjectMapper objectMapper() {
//        return new ObjectMapper();
//    }

}