package com.ustsinau.springsecuritykeycloakapi;


import com.ustsinau.springsecuritykeycloakapi.config.MyKeycloakContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestPropertySource(locations = "classpath:application-test.yml")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
        ,classes = MyKeycloakContainerConfig.class)
@ActiveProfiles("test")
class SpringSecurityKeycloakApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
