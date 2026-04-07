package com.library.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Book Service 路由
                .route("book-service", r -> r
                        .path("/api/v1/books/**")
                        .uri("http://localhost:8105")
                )
                // User Service 路由
                .route("user-service", r -> r
                        .path("/api/v1/users/**")
                        .uri("http://localhost:8106")
                )
                // Borrow Service 路由（借阅服务在book-service中）
                .route("borrow-service", r -> r
                        .path("/api/v1/borrow/**")
                        .uri("http://localhost:8105")
                )
                .build();
    }
}
