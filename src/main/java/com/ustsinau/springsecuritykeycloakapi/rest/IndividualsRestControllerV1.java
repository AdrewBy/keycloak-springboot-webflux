package com.ustsinau.springsecuritykeycloakapi.rest;


import com.ustsinau.dto.IndividualDto;
import com.ustsinau.dto.PaginatedResponseDto;
import com.ustsinau.springsecuritykeycloakapi.service.IndividualBdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/individuals")
public class IndividualsRestControllerV1 {

    private final IndividualBdService individualBdService;

    @GetMapping("/{id}")
    public Mono<IndividualDto> getIndividualById(@PathVariable String id, @RequestHeader("Authorization") String authHeader) {
        String accessToken = authHeader.replace("Bearer ", "");
        return individualBdService.getIndividualById(id, accessToken);
    }

    @PostMapping
    public Mono<IndividualDto> updateIndividual(@RequestBody IndividualDto request) {
        return individualBdService.updateIndividual(request);
    }

    @DeleteMapping("/hard-delete/{id}")
    public Mono<Void> deleteHardIndividual(@PathVariable String id) {
        return individualBdService.deleteHardIndividual(id);
    }

    @DeleteMapping("/soft-delete/{id}")
    public Mono<IndividualDto> deleteSoftIndividual(@PathVariable String id) {
        return individualBdService.deleteSoftIndividualById(id);
    }

    @GetMapping
    public Mono<PaginatedResponseDto<IndividualDto>> getAllIndividuals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return individualBdService.getAllIndividuals(page, size);
    }

}
