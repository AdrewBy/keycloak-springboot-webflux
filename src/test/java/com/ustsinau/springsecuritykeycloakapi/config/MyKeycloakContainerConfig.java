package com.ustsinau.springsecuritykeycloakapi.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.wait.strategy.Wait;


import java.time.Duration;
import java.util.List;

@Testcontainers
@TestConfiguration(proxyBeanMethods = false)
public class MyKeycloakContainerConfig {

    @Bean
    public GenericContainer<?> keycloakContainer() {
        // Запуск контейнера Keycloak с настройками
        GenericContainer<?> keycloak = new GenericContainer<>(DockerImageName.parse("quay.io/keycloak/keycloak:26.0.5"))
                .withExposedPorts(8080)  // Порт, который будет открыт внутри контейнера
                .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
                .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin")
                .withEnv("REALM_ALCHIM_CLIENT_ID","reactive-springsecurity-client")
                .withEnv("REALM_ALCHIM_CLIENT_SECRET","YoGAo0Qb7H7sdv3h4MoIFTOIqI69pEb6")
                .withCommand("start-dev", "--import-realm")  // Старт в режиме разработки с импортом realm
                .withFileSystemBind("./src/main/resources/keycloak/import/realm-alchim.json"
                        , "/opt/keycloak/data/import/realm-alchim.json"
                        , org.testcontainers.containers.BindMode.READ_ONLY)  // Монтируем файл для импорта realm
                .waitingFor(Wait.forHttp("/realms/alchim")
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(5)));  // Увеличиваем время ожидания


        // Установка порта, на котором будет доступен Keycloak
        keycloak.setPortBindings(List.of("8883:8080"));
        return keycloak;
    }
}

