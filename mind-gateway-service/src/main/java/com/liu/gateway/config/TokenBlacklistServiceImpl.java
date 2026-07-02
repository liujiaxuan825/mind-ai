package com.liu.gateway.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TOKEN_BLACK_PREFIX = "token:black:";

    @Override
    public void addCurrUserBlacklist(String token, Long remainingTime) {
        if (remainingTime <= 0) {
            log.info("token已经过期！无需加入黑名单。");
            return;
        }
        try {
            String key = TOKEN_BLACK_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", remainingTime, TimeUnit.SECONDS);
            log.debug("Token已加入黑名单，剩余时间: {}秒", remainingTime);
        } catch (Exception e) {
            log.error("加入黑名单失败: {}", e.getMessage());
            throw new RuntimeException("登出失败，请重试");
        }
    }

    @Override
    public boolean isInBlacklist(String token) {
        try {
            String key = TOKEN_BLACK_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查黑名单失败: {}", e.getMessage());
            return false;
        }
    }
}