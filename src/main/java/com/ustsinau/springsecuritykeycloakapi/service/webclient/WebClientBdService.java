package com.ustsinau.springsecuritykeycloakapi.service.webclient;


import com.ustsinau.dto.AuthRequestRegistrationDto;
import com.ustsinau.dto.IndividualDto;
import com.ustsinau.dto.PaginatedResponseDto;
import com.ustsinau.springsecuritykeycloakapi.exception.UserWithEmailAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebClientBdService {

    private final WebClient webClientDB;

    public Mono<IndividualDto> registerUserInBd(AuthRequestRegistrationDto request) {

        return webClientDB.post()
                .uri("/api/v1/individuals/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(IndividualDto.class) // Преобразуем ответ в DTO
                .doOnSuccess(response -> log.info("User successfully registered in the BD service: {}", response))
                .doOnError(error -> log.error("Failed to register user in the BD service", error))
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                        return Mono.error(new UserWithEmailAlreadyExistsException("User already exists in BD with this email","DUPLICATE_EMAIL"));
                    }
                    return Mono.error(ex);
                });
    }

    public Mono<IndividualDto> getIndividualById(String userId, String accessToken) {

        return webClientDB.get()
                .uri("/api/v1/individuals/" + userId)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(IndividualDto.class)
                .doOnSuccess(response -> log.info("Individual is successfully gotten from the BD: {}", response))
                .doOnError(error -> log.error("Failed to get individual from the BD ", error));
    }

    public Mono<IndividualDto> updateIndividual(IndividualDto request, String accessToken) {

        return webClientDB.post()
                .uri("/api/v1/individuals")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(IndividualDto.class)
                .doOnSuccess(response -> log.info("Individual successfully updated in the BD: {}", response))
                .doOnError(error -> log.error("Failed to update individual in the BD", error));
    }

    public Mono<Void> hardDeleteIndividualInBd(String id, String accessToken) {

        return webClientDB.delete()
                .uri("/api/v1/individuals/hard-delete/" + id)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(response -> log.info("Individual successfully deleted in the BD service."));

    }

    public Mono<IndividualDto> softDeleteIndividualById(String id, String accessToken) {

        return webClientDB.delete()
                .uri("/api/v1/individuals/soft-delete/" + id)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(IndividualDto.class)
                .doOnSuccess(response -> log.info("Individual successfully soft-deleted in the BD: {}", response))
                .doOnError(error -> log.error("Failed to soft-deleted individual in the BD", error));
    }

    public Mono<PaginatedResponseDto<IndividualDto>> getAll(int page, int size, String accessToken) {
        return webClientDB.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/individuals")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PaginatedResponseDto<IndividualDto>>() {})
                .doOnSuccess(response -> log.info("Individual is successfully gotten from the BD"))
                .doOnError(error -> log.error("Failed to get individual from the BD ", error));
    }


}

