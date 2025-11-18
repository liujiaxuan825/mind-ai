package com.yourname.mind.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret = "mind-ai-jwt-secret-key-2025-spring-boot-3-security";
    private long expiration = 604800000L; // 7天
    private String tokenPrefix = "Bearer ";
    private String header = "Authorization";
}