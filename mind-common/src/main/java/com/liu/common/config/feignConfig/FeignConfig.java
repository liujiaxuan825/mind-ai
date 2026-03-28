package com.liu.common.config.feignConfig;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            
            // 1. 获取当前请求的上下文（Spring 自动存的）
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                // 2. 拿到当前的 Http 请求
                HttpServletRequest request = attributes.getRequest();

                // 3. 从请求头里拿出网关传过来的 用户信息
                String userId = request.getHeader("userId");
                String username = request.getHeader("username");

                // 4. 把用户信息塞进 Feign 要发出去的请求里
                if (userId != null) template.header("userId", userId);
                if (username != null) template.header("username", username);
            }
        };
    }
}