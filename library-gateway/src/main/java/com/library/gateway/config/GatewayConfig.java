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
                        .uri("lb://book-service")
                )
                // User Service 路由
                .route("user-service", r -> r
                        .path("/api/v1/users/**")
                        .uri("lb://user-service")
                )
                // Borrow Service 路由（借阅服务在book-service中）
                .route("borrow-service", r -> r
                        .path("/api/v1/borrow/**")
                        .uri("lb://book-service")
                )
                // AI Service 路由
                .route("ai-service", r -> r
                        .path("/api/v1/ai/**")
                        .uri("lb://ai-service")
                )
                // Stats Service 路由（统计服务在book-service中）
                .route("stats-service", r -> r
                        .path("/api/v1/stats/**")
                        .uri("lb://book-service")
                )
                .build();
    }
}