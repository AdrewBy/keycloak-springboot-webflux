package com.ustsinau.springsecuritykeycloakapi;

import org.springframework.boot.SpringApplication;

public class TestSpringSecurityKeycloackApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpringSecurityKeycloakApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
