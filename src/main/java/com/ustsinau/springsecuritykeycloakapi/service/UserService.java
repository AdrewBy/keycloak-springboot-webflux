package com.ustsinau.springsecuritykeycloakapi.service;

import com.ustsinau.springsecuritykeycloakapi.service.webclient.WebClientKeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final WebClientKeycloakService webClientKeycloakService;

    public Mono<Map<String, Object>> getUserInfoFromKeycloak(String userId, String accessToken) {

        return webClientKeycloakService.getUserInfoFromKeycloak(userId, accessToken);
    }


}
