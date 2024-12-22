package com.ustsinau.springsecuritykeycloakapi.service.webclient;


import com.ustsinau.springsecuritykeycloakapi.exception.UserEmailOrPasswordException;
import com.ustsinau.springsecuritykeycloakapi.exception.UserWithEmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebClientKeycloakService {

    @Value("${REALM_ALCHIM_CLIENT_ID}")
    private String clientId;

    @Value("${REALM_ALCHIM_CLIENT_SECRET}")
    private String clientSecret;

    private final WebClient keycloakWebClient;


    public Mono<Map<String, Object>> getUserInfoFromKeycloak(String userId, String accessToken) {
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

    public Mono<Void> registerUserInKeycloak(String email, String password) {

        return getAdminAccessToken()
                .flatMap(token -> {
                    // Настройка данных для создания нового пользователя
                    Map<String, Object> userRequest = Map.of(
                            "username", email,
                            "email", email,
                            "enabled", true,
                            "credentials", List.of(Map.of(
                                    "type", "password",
                                    "value", password,
                                    "temporary", false
                            ))
                    );

                    // Отправляем запрос на создание пользователя
                    return keycloakWebClient
                            .post()
                            .uri("/admin/realms/alchim/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer " + token)
                            .bodyValue(userRequest)
                            .retrieve()
                            .bodyToMono(Void.class) // Поскольку создание пользователя не возвращает токен, ожидаем пустой ответ
                            .doOnSuccess(response -> log.info("Registration successful."))
                            .onErrorResume(e ->
                                    Mono.error(new UserWithEmailAlreadyExistsException("Registration failed: User with this email already exists", "USER_DUPLICATE_EMAIL")))
                            ;
                });
    }

    public Mono<Map<String, String>> authenticateUserInKeycloak(String email, String password) {

        log.info("Аутентификация пользователя с email: {}", email);

        MultiValueMap<String, String> credentials = new LinkedMultiValueMap<>();
        credentials.add("client_id", clientId);
        credentials.add("username", email);
        credentials.add("password", password);
        credentials.add("grant_type", "password");
        credentials.add("client_secret", clientSecret);

        return keycloakWebClient.post()
                .uri( "/realms/alchim/protocol/openid-connect/token")
                .body(BodyInserters.fromFormData(credentials))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .onErrorResume(e ->
                        Mono.error(new UserEmailOrPasswordException("Login failed: Invalid email or password", "USER_INVALID_EMAIL_OR_PASSWORD")))
                .doOnSuccess(response -> log.info("Аутентификация успешна. Access Token получен"))
                .doOnError(error -> log.error("Ошибка аутентификации: {}", error.getMessage()));
    }

    public Mono<Map<String, String>> refreshToken(String refreshToken) {
        // Логирование запроса
        log.info("Начинаем процесс обновления токена с refreshToken: {}", refreshToken);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", refreshToken);

        return keycloakWebClient.post()
                .uri("/realms/alchim//protocol/openid-connect/token")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .doOnSuccess(response -> {
                    // Логируем успешный ответ
                    log.info("Токен успешно обновлен, получен новый токен: {}", response.get("access_token"));
                })
                .doOnError(error -> {
                    // Логируем ошибку, если что-то пошло не так
                    log.error("Ошибка при обновлении токена: {}", error.getMessage());
                });
    }

    public Mono<String> getAdminAccessToken() {

        // Запрос на получение токена администратора через client_credentials
        return keycloakWebClient
                .post()
                .uri("/realms/alchim/protocol/openid-connect/token")
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", clientId)  // Укажите ваш client_id
                        .with("client_secret", clientSecret)) // Укажите ваш client_secret
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"));
    }
}

