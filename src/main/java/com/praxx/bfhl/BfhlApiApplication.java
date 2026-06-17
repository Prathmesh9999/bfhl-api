package com.praxx.bfhl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class BfhlApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BfhlApiApplication.class, args);
    }
}
