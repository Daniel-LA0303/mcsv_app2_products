package com.mcsv.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    //config to comunicate with inventory-service we need webflux in pom file
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
