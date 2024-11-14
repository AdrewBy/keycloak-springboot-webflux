package com.ustsinau.springsecuritykeycloakapi.rest;

import com.ustsinau.springsecuritykeycloakapi.dto.RegisterRequest;
import com.ustsinau.springsecuritykeycloakapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRestControllerV1 {

    private final AuthService authService;


    @PostMapping("/registration")
    public Mono<Map<String, String>> register(@RequestBody RegisterRequest request) {

        // Извлекаем параметры из DTO
        String email = request.getEmail();
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        // Проверяем совпадение паролей
        if (!password.equals(confirmPassword)) {
            return Mono.error(new IllegalArgumentException("Passwords do not match"));
        }

        // Вызываем метод регистрации пользователя в Keycloak
        return authService.registerUser(email, password)
                .map(response -> Map.of("message", "User registered successfully"))
                .onErrorResume(e -> Mono.just(Map.of("error", "Registration failed: " + e.getMessage())));
    }


    @PostMapping("/login")
    public Mono<Map<String, String>> login(@RequestBody RegisterRequest request) {


        String email = request.getEmail();
        String password = request.getPassword();
        log.info("Запрос на вход пользователя с email: {}", email);

        return authService.authenticateUser(email, password)
                .doOnError(e -> log.error("Ошибка при входе: {}", e.getMessage()));
    }

    @PostMapping("/refresh-token")
    public Mono<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refresh_token");

        return authService.refreshToken(refreshToken);
    }

}
