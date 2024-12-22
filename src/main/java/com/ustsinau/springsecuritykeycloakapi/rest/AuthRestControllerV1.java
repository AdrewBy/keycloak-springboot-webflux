package com.ustsinau.springsecuritykeycloakapi.rest;

import com.ustsinau.dto.AuthRequestRegistrationDto;
import com.ustsinau.springsecuritykeycloakapi.dto.RegisterRequest;
import com.ustsinau.springsecuritykeycloakapi.exception.ConfirmPasswordException;
import com.ustsinau.springsecuritykeycloakapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRestControllerV1 {

    private final AuthService authService;

    @PostMapping("/registration-bd-keycloak")
    public Mono<ResponseEntity<Map<String, Object>>> register(@RequestBody AuthRequestRegistrationDto request) {

        String email = request.getUser().getEmail();
        String password = request.getPassword();

        return authService.registerUserInBdAppAndKeycloak(request)
                .then(Mono.defer(() -> authService.authenticateUserInKeycloak(email, password))) // После регистрации аутентифицируем пользователя для получения токенов
                .doOnSuccess(response -> log.info("Authentication successful."))
                .map(response -> {
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(getResponseBody(response));

                });
    }

    // created only for test
    @PostMapping("/registration-keycloak")
    public Mono<ResponseEntity<Map<String, Object>>> register(@RequestBody RegisterRequest request) {

        String email = request.getEmail();
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        if (!password.equals(confirmPassword)) {
            return Mono.error(new ConfirmPasswordException("Password confirmation does not match", "PASSWORD_NOT_MATCH"));

        }

        return authService.registerUserInKeycloak(email,password)
                .then(Mono.defer(() -> authService.authenticateUserInKeycloak(email, password))) // После регистрации аутентифицируем пользователя для получения токенов
                .doOnSuccess(response -> log.info("Authentication successful."))
                .map(response -> {
                    return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(getResponseBody(response));

                });
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(@RequestBody RegisterRequest request) {

        String email = request.getEmail();
        String password = request.getPassword();
        log.info("Запрос на вход пользователя с email: {}", email);

        return authService.authenticateUserInKeycloak(email, password)
                .map(response -> {
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(getResponseBody(response));
                });
    }

    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<Map<String, Object>>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refresh_token");

        return authService.refreshToken(refreshToken)
                .map(response ->{
                      return   ResponseEntity
                        .status(HttpStatus.OK)
                .body(getResponseBody(response));
                });
    }

    private Map<String, Object> getResponseBody(Map<String, String> response) {
        Map<String, Object> responseMap = new LinkedHashMap<>();
        responseMap.put("access_token", response.get("access_token"));
        responseMap.put("expires_in", Integer.parseInt(response.get("expires_in")));
        responseMap.put("refresh_token", response.get("refresh_token"));
        responseMap.put("token_type", response.get("token_type"));
        return responseMap;
    }
}
