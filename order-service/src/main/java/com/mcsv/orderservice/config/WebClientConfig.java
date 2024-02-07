package com.mcsv.orderservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    //config to comunicate with inventory-service we need webflux in pom file
    @Bean
    @LoadBalanced //its a charge balancer to comunicate with other services
    public WebClient.Builder webClient() {
        return WebClient.builder();
    }
}