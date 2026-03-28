package com.liu.common.untils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserContext {

    // 获取当前请求的request
    private static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    // 获取用户ID
    public static Long getUserId() {
        String userId = getRequest().getHeader("userId");
        return userId == null ? null : Long.valueOf(userId);
    }

    // 获取用户名
    public static String getUsername() {
        return getRequest().getHeader("username");
    }


    // 获取token
    public static String getToken() {
        return getRequest().getHeader("token");
    }
}