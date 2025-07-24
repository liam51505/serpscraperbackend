package com.liamcashman.serpscrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SerpScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(SerpScrapperApplication.class, args);
    }
}
