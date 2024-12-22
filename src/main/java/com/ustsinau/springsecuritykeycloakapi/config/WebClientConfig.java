package com.ustsinau.springsecuritykeycloakapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${BASE_URL_JPA_DB}")
    private String baseUrlJpaDb;

    @Value("${BASE_URL_KEYCLOAK}")
    private String baseUrlKeycloak;

    @Bean
    public WebClient keycloakWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrlKeycloak)
                .build();
    }

    @Bean
    public WebClient webClientDB(WebClient.Builder builder) {
        return builder
                .baseUrl(baseUrlJpaDb)
                .build();
    }

}

