package com.liu.ai.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserContext {

    private static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    public static Long getUserId() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        String userId = request.getHeader("userId");
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(userId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String getUsername() {
        HttpServletRequest request = getRequest();
        return request == null ? null : request.getHeader("username");
    }

    public static String getToken() {
        HttpServletRequest request = getRequest();
        return request == null ? null : request.getHeader("token");
    }
}