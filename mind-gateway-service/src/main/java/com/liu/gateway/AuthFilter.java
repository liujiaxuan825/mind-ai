package com.liu.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liu.gateway.config.JwtUtils;
import com.liu.gateway.config.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;

    private final ObjectMapper objectMapper;

    private final TokenBlacklistService tokenBlacklistService;

    private static final String[] WHITE_LIST = {
            "/api/user/login",
            "/api/user/register"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        for (String s : WHITE_LIST) {
            if (path.startsWith(s)) {
                log.info("白名单路径放行: {}", path);
                return chain.filter(exchange);
            }
        }

        String auth = request.getHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            log.warn("缺少或无效的Authorization令牌: {}", path);
            return unauthorized(exchange, "缺少或无效的Authorization令牌");
        }

        String token = auth.substring(7);
        if (tokenBlacklistService.isInBlacklist(token)) {
            log.warn("拒绝已登出的token访问: {}", path);
            return unauthorized(exchange, "登录已失效，请重新登录");
        }

        Long userId;
        String username;
        try{
            userId = jwtUtils.getUserIdFromToken(token);
            username = jwtUtils.getUsernameFromToken(token);
        }catch (Exception e){
            log.error("Token解析失败: {}", e.getMessage(), e);
            return unauthorized(exchange, "令牌无效或已过期");
        }
        ServerHttpRequest newRequest = request.mutate()
                .header("userId", userId.toString())
                .header("username", username)
                .header("token", token)
                .build();
        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        log.info("用户 {} 访问路径 {}", username, path);

        return chain.filter(newExchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("code", 40100);
        result.put("message", msg);
        result.put("timestamp", System.currentTimeMillis());

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
    @Override
    public int getOrder() {
        return -100;
    }
}