package com.lexsecura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LexSecuraApplication {

    public static void main(String[] args) {
        SpringApplication.run(LexSecuraApplication.class, args);
    }
}
