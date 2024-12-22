package com.ustsinau.springsecuritykeycloakapi;


import com.ustsinau.springsecuritykeycloakapi.config.MyKeycloakContainerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest
@Testcontainers
@TestPropertySource(locations = "classpath:application-test.yml")
public class TestSpringSecurityKeycloakApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpringSecurityKeycloakApiApplication::main)
                .with(MyKeycloakContainerConfig.class)
                .run(args);

    }

}
