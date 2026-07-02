package com.liu.gateway.config;

public interface TokenBlacklistService {
    void addCurrUserBlacklist(String token, Long remainingTime);
    boolean isInBlacklist(String token);
}