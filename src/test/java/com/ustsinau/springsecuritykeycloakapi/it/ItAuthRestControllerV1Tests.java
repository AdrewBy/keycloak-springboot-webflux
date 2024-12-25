package com.ustsinau.springsecuritykeycloakapi.it;


import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ustsinau.dto.AuthRequestRegistrationDto;
import com.ustsinau.dto.IndividualDto;
import com.ustsinau.springsecuritykeycloakapi.config.MyKeycloakContainerConfig;
import com.ustsinau.springsecuritykeycloakapi.dto.RegisterRequest;
import com.ustsinau.springsecuritykeycloakapi.service.AuthService;
import com.ustsinau.springsecuritykeycloakapi.service.webclient.WebClientBdService;
import com.ustsinau.springsecuritykeycloakapi.service.webclient.WebClientKeycloakService;
import com.ustsinau.springsecuritykeycloakapi.utils.JsonUtils;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
        ,classes = MyKeycloakContainerConfig.class)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ItAuthRestControllerV1Tests {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private WebClient keycloakWebClient;

    @Autowired
    private AuthService authService;

    @Autowired
    private WebClientKeycloakService webClientKeycloakService;

    @MockBean
    private WebClientBdService webClientBdService;


    @BeforeEach
    public void deleteAllUsers() {

        String adminToken = webClientKeycloakService.getAdminAccessToken().block();
        // Получаем список всех пользователей
        List<String> userIds = keycloakWebClient.get()
                .uri("/admin/realms/alchim/users")
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .bodyToFlux(Map.class)
                .map(user -> user.get("id").toString())
                .collectList()
                .block();

        // Удаляем каждого пользователя по его ID
        userIds.forEach(userId -> {
            keycloakWebClient.delete()
                    .uri("/admin/realms/alchim/users/{userId}", userId)
                    .header("Authorization", "Bearer " + adminToken)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        });
    }

    @Test
    @DisplayName("Test registration user bd-keycloak functionality")
    public void givenUser_whenRegisterBdAndKeycloakUser_thenSuccessResponse() {

        AuthRequestRegistrationDto authDto = JsonUtils.readJsonFromFile("src/test/resources/json/authDto.json", AuthRequestRegistrationDto.class);
        IndividualDto individualDto = JsonUtils.readJsonFromFile("src/test/resources/json/individualDto.json", IndividualDto.class);


        BDDMockito.given(webClientBdService.registerUserInBd(any(AuthRequestRegistrationDto.class)))
                .willReturn(Mono.just(individualDto));

        // Когда
        WebTestClient.ResponseSpec result = webTestClient
                .post()
                .uri("/api/v1/auth/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(authDto), AuthRequestRegistrationDto.class)
                .exchange();

        // Тогда
        result.expectStatus().isCreated()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.access_token").isNotEmpty()
                .jsonPath("$.expires_in").isEqualTo(3600)
                .jsonPath("$.refresh_token").isNotEmpty()
                .jsonPath("$.token_type").isEqualTo("Bearer");
    }

    @Test
    @DisplayName("Test user login functionality")
    public void givenValidLoginRequest_whenLoginUser_thenSuccessResponse() {

        authService.registerUserInKeycloak("john.doe@mail.com", "password123").block();

        // Создаем объект запроса для логина
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john.doe@mail.com");
        request.setPassword("password123");

        // Когда
        WebTestClient.ResponseSpec result = webTestClient
                .post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), RegisterRequest.class)
                .exchange();

        // Тогда
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.access_token").isNotEmpty()
                .jsonPath("$.expires_in").isEqualTo(3600)
                .jsonPath("$.refresh_token").isNotEmpty()
                .jsonPath("$.token_type").isEqualTo("Bearer");
    }

    @Test
    @DisplayName("Test refresh token functionality")
    public void givenValidRefreshToken_whenRefreshToken_thenSuccessResponse() {

        authService.registerUserInKeycloak("john.doe@mail.com", "password123").block();

        String refreshToken = authService.authenticateUserInKeycloak("john.doe@mail.com", "password123")
                .map(resp -> resp.get("refresh_token"))
                .block();

        // Создаем объект запроса для обновления токена
        Map<String, String> request = new HashMap<>();
        request.put("refresh_token", refreshToken);

        // Когда
        WebTestClient.ResponseSpec result = webTestClient
                .post()
                .uri("/api/v1/auth/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), Map.class)
                .exchange();

        // Тогда
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.access_token").isNotEmpty()
                .jsonPath("$.expires_in").isEqualTo(3600)
                .jsonPath("$.refresh_token").isNotEmpty()
                .jsonPath("$.token_type").isEqualTo("Bearer");
    }

    @Test
    @DisplayName("Test get user information functionality")
    public void givenUserId_whenGetUserInfo_thenSuccessResponse() {

        authService.registerUserInKeycloak("john.doe@mail.com", "password123").block();

        // Аутентификация и получение токена
        String token = authService.authenticateUserInKeycloak("john.doe@mail.com", "password123")
                .map(getToken->getToken.get("access_token"))
                .block().toString();

        // Извлечение sub из токена
        String userId = extractSubFromToken(token);

        String accessToken = webClientKeycloakService.getAdminAccessToken().block();

        // Запрос информации о пользователе
        WebTestClient.ResponseSpec getResult = webTestClient
                .get()
                .uri("/api/v1/users/{id}", userId)
                .header("Authorization", "Bearer " + accessToken)
                .exchange();


        // Проверяем, что статус ответа успешный и данные о пользователе верны
        getResult.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.email").isEqualTo("john.doe@mail.com")
                .jsonPath("$.id").isEqualTo(userId);
    }

    private String extractSubFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getSubject(); // Получаем значение sub
    }


}
