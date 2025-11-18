package com.yourname.mind.config;

import com.yourname.mind.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证拦截器
 * 负责拦截请求并设置用户上下文
 */
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;
    private final TokenBlacklistService tokenBlacklistService;
    
    // 修正：使用正确的 SLF4J Logger
    private static final Logger log = LoggerFactory.getLogger(AuthenticationInterceptor.class);
    
    // 排除认证的路径
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
        "/user/login",
        "/user/register"
    );
    
    // 添加构造函数注入
    public AuthenticationInterceptor(JwtUtils jwtUtils, ObjectMapper objectMapper, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
        this.tokenBlacklistService = tokenBlacklistService;
    }
    
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        
        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        // 修正：添加日志参数
        log.info("拦截请求: {} {}", method, requestUri);

        // 检查是否在排除路径中
        if (isExcludePath(requestUri)) {
            return true;
        }
        
        // 从请求头获取token
        String token = extractToken(request);
        if (token == null) {
            handleUnauthorized(response, "缺少访问令牌");
            return false;
        }
        if(tokenBlacklistService.isInBlacklist(token)) {
            log.warn("拒绝已登出的token访问: {}", requestUri);
            handleUnauthorized(response, "登录已失效，请重新登录");
            return false;
        }

        
        try {
            // 解析token并设置用户上下文
            UserContextHolder.UserContext userContext = parseToken(token);
            UserContextHolder.setUserContext(userContext);
            
            // 修正：添加日志参数
            log.info("设置用户上下文: {}", userContext.getUsername());
            
        } catch (Exception e) {
            // 修正：添加日志参数和错误信息
            log.error("Token解析失败: {}", e.getMessage(), e);
            handleUnauthorized(response, "令牌无效或已过期");
            return false;
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                              HttpServletResponse response, 
                              Object handler, Exception ex) {
        // 请求完成后清理用户上下文，防止内存泄漏
        UserContextHolder.clear();
        log.info("清理用户上下文");
    }
    
    /**
     * 检查是否为排除路径
     */
    private boolean isExcludePath(String requestUri) {
        return EXCLUDE_PATHS.stream().anyMatch(requestUri::startsWith);
    }
    
    /**
     * 从请求头提取token
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * 解析token
     */
    private UserContextHolder.UserContext parseToken(String token) {
        // 验证token
        boolean isValid = jwtUtils.validateToken(token);
        if (!isValid) {
            throw new RuntimeException("令牌验证失败！");
        }

        // 从token中解析用户信息
        Long userId = jwtUtils.getUserIdFromToken(token);
        String username = jwtUtils.getUsernameFromToken(token);

        UserContextHolder.UserContext userContext =
            new UserContextHolder.UserContext(userId, username, token);

        return userContext;
    }
    
    /**
     * 处理未授权响应
     */
    private void handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", 40100);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        // 使用注入的 ObjectMapper
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}