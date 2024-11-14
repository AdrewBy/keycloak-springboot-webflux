package com.ustsinau.springsecuritykeycloackapi;

import org.springframework.boot.SpringApplication;

public class TestSpringSecurityKeycloackApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpringSecurityKeycloackApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
