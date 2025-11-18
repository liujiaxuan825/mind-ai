package com.yourname.mind.config;

/**
 * 用户上下文持有器
 * 用于在当前线程中存储和获取用户信息
 */
public class UserContextHolder {
    
    private static final ThreadLocal<UserContext> USER_CONTEXT = new ThreadLocal<>();
    
    /**
     * 设置当前用户上下文
     */
    public static void setUserContext(UserContext userContext) {
        USER_CONTEXT.set(userContext);
    }
    
    /**
     * 获取当前用户上下文
     */
    public static UserContext getUserContext() {
        return USER_CONTEXT.get();
    }
    
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        UserContext context = getUserContext();
        return context != null ? context.getUserId() : null;
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        UserContext context = getUserContext();
        return context != null ? context.getUsername() : null;
    }
    
    /**
     * 清除用户上下文
     */
    public static void clear() {
        USER_CONTEXT.remove();
    }
    
    /**
     * 用户上下文对象
     */
    public static class UserContext {
        private Long userId;
        private String username;
        private String token;
        
        public UserContext() {}
        
        public UserContext(Long userId, String username, String token) {
            this.userId = userId;
            this.username = username;
            this.token = token;
        }
        
        // Getter and Setter
        public Long getUserId() {
            return userId;
        }
        
        public void setUserId(Long userId) {
            this.userId = userId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
        
        @Override
        public String toString() {
            return "UserContext{" +
                    "userId='" + userId + '\'' +
                    ", username='" + username + '\'' +
                    '}';
        }
    }
}