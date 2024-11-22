package com.ustsinau.springsecuritykeycloakapi.rest;


import com.ustsinau.springsecuritykeycloakapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserRestControllerV1 {

    private final UserService userService;

    @GetMapping("/{id}")
    public Mono<Map<String, Object>> getUser(@PathVariable String id, @RequestHeader("Authorization") String authHeader) {
        // Извлекаем access_token из заголовка запроса
        String accessToken = authHeader.replace("Bearer ", "");

        return userService.getUserInfo(id, accessToken);
    }
}

