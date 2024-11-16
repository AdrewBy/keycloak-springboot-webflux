package com.ustsinau.springsecuritykeycloakapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Service
public class UserService {

    private final WebClient keycloakWebClient;

    public UserService(WebClient keycloakWebClient) {
        this.keycloakWebClient = keycloakWebClient;
    }

    // Получение данных пользователя по ID
    public Mono<Map<String, Object>> getUserInfo(String userId, String accessToken) {
        log.info("Запрашиваем информацию о пользователе с ID: {} с использованием accessToken", userId);

        return keycloakWebClient.get()
                .uri("/admin/realms/alchim/users/{id}", userId)
                .headers(headers -> headers.setBearerAuth(accessToken)) // Используем access_token для авторизации
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    // Извлекаем данные и формируем новый объект ответа
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", response.get("id"));
                    userInfo.put("email", response.get("email"));
                    userInfo.put("created_at", new Date((Long) response.get("createdTimestamp")).toInstant().toString());

                    // Получаем роли
//                    List<String> roles = extractRoles(response);
//                    userInfo.put("roles", roles);

                    return userInfo;
                })
                .doOnSuccess(response -> {
                    log.info("Информация о пользователе с ID {} успешно получена: {}", userId, response);
                })
                .doOnError(error -> {
                    log.error("Ошибка при получении информации о пользователе с ID {}: {}", userId, error.getMessage());
                });
    }

}
