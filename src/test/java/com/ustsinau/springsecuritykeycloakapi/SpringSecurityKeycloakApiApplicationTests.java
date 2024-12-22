package com.ustsinau.springsecuritykeycloakapi;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestPropertySource(locations = "classpath:application-test.yml")
@SpringBootTest
class SpringSecurityKeycloakApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
