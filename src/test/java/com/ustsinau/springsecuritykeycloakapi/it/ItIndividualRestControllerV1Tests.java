package com.ustsinau.springsecuritykeycloakapi.it;


import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ustsinau.dto.IndividualDto;
import com.ustsinau.dto.PaginatedResponseDto;
import com.ustsinau.springsecuritykeycloakapi.config.MyKeycloakContainerConfig;
import com.ustsinau.springsecuritykeycloakapi.service.AuthService;
import com.ustsinau.springsecuritykeycloakapi.service.IndividualBdService;
import com.ustsinau.springsecuritykeycloakapi.service.webclient.WebClientBdService;
import com.ustsinau.springsecuritykeycloakapi.service.webclient.WebClientKeycloakService;
import com.ustsinau.springsecuritykeycloakapi.utils.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;


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

        String token = getToken();

        IndividualDto individualDto = JsonUtils.readJsonFromFile("src/test/resources/json/individualDto.json", IndividualDto.class);
        String id = individualDto.getId();

        BDDMockito.given(webClientBdService.getIndividualById(any(String.class), any(String.class)))
                .willReturn(Mono.just(individualDto));

        // Когда
        WebTestClient.ResponseSpec result = webTestClient
                .get()
                .uri("/api/v1/individuals/{id}", id)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // Тогда
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.email").isEqualTo("user3@example.com")
                .jsonPath("$.id").isEqualTo(id);
    }

    @Test
    @DisplayName("Test update individual functionality")
    public void givenIndividual_whenUpdateIndividual_thenSuccessResponse() {

        String token = getToken();

        IndividualDto individualDto = JsonUtils.readJsonFromFile("src/test/resources/json/individualDto.json", IndividualDto.class);
        individualDto.setPhoneNumber("3434");

        BDDMockito.given(webClientBdService.updateIndividual(any(IndividualDto.class), any(String.class)))
                .willReturn(Mono.just(individualDto));

        // Когда
        WebTestClient.ResponseSpec result = webTestClient
                .post()
                .uri("/api/v1/individuals")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(individualDto)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // Тогда
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.email").isEqualTo("user3@example.com")
                .jsonPath("$.phoneNumber").isEqualTo("3434");
    }

    @Test
    @DisplayName("Test hard delete individual functionality")
    public void givenIndividualId_whenHardDeleteIndividual_thenSuccessResponse() {

        String token = getToken();

        IndividualDto individualDto = JsonUtils.readJsonFromFile("src/test/resources/json/individualDto.json", IndividualDto.class);
        String id = individualDto.getId();

        BDDMockito.given(webClientBdService.hardDeleteIndividualInBd(any(String.class), any(String.class)))
                .willReturn(Mono.empty());

        // Когда
        WebTestClient.ResponseSpec result = webTestClient
                .delete()
                .uri("/api/v1/individuals/hard-delete/{id}", id)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // Тогда
        result.expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("Test soft delete individual functionality")
    public void givenIndividualId_whenSoftDeleteIndividual_thenSuccessResponse() {

        String token = getToken();

        IndividualDto individualDto = JsonUtils.readJsonFromFile("src/test/resources/json/individualDto.json", IndividualDto.class);
        String id = individualDto.getId();

        BDDMockito.given(webClientBdService.softDeleteIndividualById(any(String.class), any(String.class)))
                .willReturn(Mono.just(individualDto));
        individualDto.setStatus("DELETED");

        // Когда
        WebTestClient.ResponseSpec result = webTestClient
                .delete()
                .uri("/api/v1/individuals/soft-delete/${id}",id)
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // Тогда
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.status").isEqualTo("DELETED");
    }


    @Test
    @DisplayName("Test get all individuals functionality")
    public void whenGetAllIndividuals_thenSuccessResponse() {

        String token = getToken();

        IndividualDto individualDto1 = JsonUtils.readJsonFromFile("src/test/resources/json/individualDto.json", IndividualDto.class);
        IndividualDto individualDto2 = JsonUtils.readJsonFromFile("src/test/resources/json/individualDto2.json", IndividualDto.class);
        PaginatedResponseDto<IndividualDto> paginatedResponse = new PaginatedResponseDto<>();
        paginatedResponse.setContent(List.of(individualDto1, individualDto2));
        paginatedResponse.setPageNumber(0);
        paginatedResponse.setPageSize(2);
        paginatedResponse.setTotalElements(2);
        paginatedResponse.setTotalPages(1);


        BDDMockito.given(webClientBdService.getAll(anyInt(), anyInt(), any(String.class)))
                .willReturn(Mono.just(paginatedResponse));


        // Когда
        WebTestClient.ResponseSpec result = webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/individuals")
                        .queryParam("page", 0)
                        .queryParam("size", 50)
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .exchange();

        // Тогда
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.content").isArray()
                .jsonPath("$.content[0].user.email").isEqualTo("user3@example.com")
                .jsonPath("$.content[1].user.email").isEqualTo("user2@example.com")
                .jsonPath("$.totalElements").isEqualTo(2)
                .jsonPath("$.totalPages").isEqualTo(1);
    }

    @NotNull
    private String getToken() {
        authService.registerUserInKeycloak("john.doe@mail.com", "password123").block();

        String token = authService.authenticateUserInKeycloak("john.doe@mail.com", "password123")
                .map(getToken -> getToken.get("access_token"))
                .block().toString();
        return token;
    }
}
