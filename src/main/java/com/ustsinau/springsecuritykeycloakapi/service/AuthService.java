package com.ustsinau.springsecuritykeycloakapi.service;

import com.ustsinau.springsecuritykeycloakapi.exception.UserWithEmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClient keycloakWebClient;

    @Value("${REALM_ALCHIM_CLIENT_ID}")
    private String clientId;

    @Value("${REALM_ALCHIM_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${REALM_MASTER_NAME}")
    private String adminName;

    @Value("${REALM_MASTER_PASSWORD}")
    private String adminPassword;

    // Регистрация пользователя
    public Mono<Map<String, String>> registerUser(String email, String password) {

        // Получаем токен администратора
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
                            .header("Authorization", "Bearer " + token)
                            .bodyValue(userRequest)
                            .retrieve()
                            .bodyToMono(Void.class) // Поскольку создание пользователя не возвращает токен, ожидаем пустой ответ
                            .doOnSuccess(response -> log.info("Registration successful."))
                            .onErrorResume(e ->
                                    Mono.error(new UserWithEmailAlreadyExistsException("Registration failed: User with this email already exists", "USER_DUPLICATE_EMAIL")))
                            .then(Mono.defer(() -> authenticateUser(email, password))); // После регистрации аутентифицируем пользователя для получения токенов
                });
    }

    // Аутентификация пользователя
    public Mono<Map<String, String>> authenticateUser(String email, String password) {

        log.info("Аутентификация пользователя с email: {}", email);

        MultiValueMap<String, String> credentials = new LinkedMultiValueMap<>();
        credentials.add("client_id", clientId);
        credentials.add("username", email);
        credentials.add("password", password);
        credentials.add("grant_type", "password");
        credentials.add("client_secret", clientSecret);

        return keycloakWebClient.post()
                .uri("/realms/alchim/protocol/openid-connect/token")
                .body(BodyInserters.fromFormData(credentials))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                });
    }


    // Обновление токена
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
        // Запрос на получение токена администратора
        return keycloakWebClient
                .post()
                .uri("/realms/master/protocol/openid-connect/token")
                .body(BodyInserters.fromFormData("client_id", "admin-cli")
                        .with("username", adminName)  // Замените на имя администратора Keycloak
                        .with("password", adminPassword)  // Замените на пароль администратора Keycloak
                        .with("grant_type", "password"))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"));
    }
}
