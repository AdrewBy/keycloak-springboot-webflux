package com.ustsinau.springsecuritykeycloakapi.service;


import com.ustsinau.dto.IndividualDto;
import com.ustsinau.dto.PaginatedResponseDto;
import com.ustsinau.springsecuritykeycloakapi.service.webclient.WebClientBdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndividualBdService {

    private final WebClientBdService webClientBdService;


    public Mono<IndividualDto> getIndividualById(String userId, String accessToken) {


        return webClientBdService.getIndividualById(userId, accessToken);
    }

    public Mono<IndividualDto> updateIndividual(IndividualDto request) {
        return webClientBdService.updateIndividual(request);
    }

    public Mono<Void> deleteHardIndividual(String id) {
        return webClientBdService.hardDeleteIndividualInBd(id);
    }

    public Mono<IndividualDto> deleteSoftIndividualById(String userId) {
        return webClientBdService.softDeleteIndividualById(userId);
    }

    public Mono<PaginatedResponseDto<IndividualDto>> getAllIndividuals(int page, int size) {
        return webClientBdService.getAll(page, size);
    }

}
