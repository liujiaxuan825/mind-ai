package com.yourname.mind.service.impl;

import com.yourname.mind.common.constant.RedisConstant;
import com.yourname.mind.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final RedisTemplate<String,Object> redisTemplate;

    public void addCurrUserBlacklist(String token,Long remainingTime) {
        if(remainingTime<=0){
            log.info("token已经过期！无需加入黑名单。");
            return;
        }
        try {
            String key = RedisConstant.TOKEN_BLACK+token;
            redisTemplate.opsForValue().set(key,"1",remainingTime, TimeUnit.SECONDS);
            log.debug("Token已加入黑名单，剩余时间: {}秒", remainingTime);
        } catch (Exception e) {
            log.error("加入黑名单失败: {}", e.getMessage());
            throw new RuntimeException("登出失败，请重试");
        }

    }

    public boolean isInBlacklist(String token) {
        try {
            String key = RedisConstant.TOKEN_BLACK + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查黑名单失败: {}", e.getMessage());
            // 如果Redis出现问题，为了安全起见，认为token在黑名单中
            return true;
        }
    }
}
