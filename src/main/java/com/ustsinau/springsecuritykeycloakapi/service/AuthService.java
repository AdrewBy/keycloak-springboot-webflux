package com.ustsinau.springsecuritykeycloakapi.service;

import com.ustsinau.dto.AuthRequestRegistrationDto;
import com.ustsinau.springsecuritykeycloakapi.exception.ConfirmPasswordException;
import com.ustsinau.springsecuritykeycloakapi.service.webclient.WebClientBdService;
import com.ustsinau.springsecuritykeycloakapi.service.webclient.WebClientKeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClientKeycloakService webClientKeycloakService;

    private final WebClientBdService webClientBdService;

    public Mono<Void> registerUserInBdAppAndKeycloak(AuthRequestRegistrationDto request) {

        String email = request.getUser().getEmail();
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        if (!password.equals(confirmPassword)) {
            return Mono.error(new ConfirmPasswordException("Password confirmation does not match", "PASSWORD_NOT_MATCH"));

        }

        return webClientBdService.registerUserInBd(request) // Шаг 1: Создаем пользователя в приложении
                .flatMap(userId -> registerUserInKeycloak(email, password) // Шаг 2: Регистрируем в Keycloak
                        .onErrorResume(error -> { // Если ошибка в Keycloak
                            log.error("Error registering user in Keycloak, rolling back user in DB", error);
                            String token = String.valueOf(webClientKeycloakService.getAdminAccessToken());
                            return webClientBdService.hardDeleteIndividualInBd(userId.getId(), token)
                                    .then(Mono.error(error)); // Пробрасываем ошибку дальше
                        })
                )
                .doOnError(error -> log.error("Error during registration process", error));

    }

    public Mono<Void> registerUserInKeycloak(String email, String password) {

        return webClientKeycloakService.registerUserInKeycloak(email, password);
    }

    public Mono<Map<String, String>> refreshToken(String refreshToken) {
        return webClientKeycloakService.refreshToken(refreshToken);
    }

    public Mono<Map<String, String>> authenticateUserInKeycloak(String email, String password) {
        return webClientKeycloakService.authenticateUserInKeycloak(email, password);
    }


}
