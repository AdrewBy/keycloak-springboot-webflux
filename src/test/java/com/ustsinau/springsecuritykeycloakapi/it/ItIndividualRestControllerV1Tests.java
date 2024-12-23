package com.ustsinau.springsecuritykeycloakapi.it;


import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ustsinau.dto.IndividualDto;
import com.ustsinau.springsecuritykeycloakapi.config.MyKeycloakContainerConfig;
import com.ustsinau.springsecuritykeycloakapi.service.AuthService;
import com.ustsinau.springsecuritykeycloakapi.service.IndividualBdService;
import com.ustsinau.springsecuritykeycloakapi.service.webclient.WebClientBdService;
import com.ustsinau.springsecuritykeycloakapi.service.webclient.WebClientKeycloakService;
import com.ustsinau.springsecuritykeycloakapi.utils.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
        , classes = MyKeycloakContainerConfig.class)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ItIndividualRestControllerV1Tests {


    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private WebClient keycloakWebClient;

    @Autowired
    private WebClientKeycloakService webClientKeycloakService;

    @Autowired
    private AuthService authService;

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
    @DisplayName("Test get individual by id functionality")
    public void givenIndividual_whenGetIndividualById_thenSuccessResponse() {

        authService.registerUserInKeycloak("john.doe@mail.com", "password123").block();

        String token = authService.authenticateUserInKeycloak("john.doe@mail.com", "password123")
                .map(getToken->getToken.get("access_token"))
                .block().toString();

        IndividualDto individualDto = JsonUtils.readJsonFromFile("src/test/resources/json/individualDto.json", IndividualDto.class);
        String id = individualDto.getId();

        BDDMockito.given(webClientBdService.getIndividualById(any(String.class),any(String.class)))
                .willReturn(Mono.just(individualDto));

        // Когда
        WebTestClient.ResponseSpec result = webTestClient
                .get()
                .uri("/api/v1/individuals/{id}",id)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // Тогда
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.email").isEqualTo("user3@example.com")
                .jsonPath("$.id").isEqualTo(id);   }

}
