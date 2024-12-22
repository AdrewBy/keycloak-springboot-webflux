package com.ustsinau.springsecuritykeycloakapi.it;


import com.ustsinau.springsecuritykeycloakapi.config.MyKeycloakContainerConfig;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
        ,classes = MyKeycloakContainerConfig.class)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ItIndividualRestControllerV1Tests {


}
