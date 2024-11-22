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

        // Основной запрос для получения базовой информации о пользователе
        Mono<Map<String, Object>> userDetails = keycloakWebClient.get()
                .uri("/admin/realms/alchim/users/{id}", userId)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});

        // Дополнительный запрос для получения ролей
        Mono<List<String>> userRoles = keycloakWebClient.get()
                .uri("/admin/realms/alchim/users/{id}/role-mappings/realm", userId)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .map(roles -> roles.stream()
                        .map(role -> (String) role.get("name")) // Извлекаем имена ролей
                        .toList()
                );

        // Последовательное выполнение запросов и формирование результата
        return userDetails.flatMap(details ->
                userRoles.map(roles -> {
                    Map<String, Object> userInfo = new LinkedHashMap<>();
                    userInfo.put("id", details.get("id"));
                    userInfo.put("email", details.get("email"));
                    userInfo.put("roles", roles);
                    userInfo.put("created_at", new Date((Long) details.get("createdTimestamp")).toInstant().toString());
                    return userInfo;
                })
        ).doOnSuccess(response -> {
            log.info("Информация о пользователе с ID {} успешно получена: {}", userId, response);
        }).doOnError(error -> {
            log.error("Ошибка при получении информации о пользователе с ID {}: {}", userId, error.getMessage());
        });
    }



}
