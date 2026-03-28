package com.liu.common.service;

public interface TokenBlacklistService {
    public void addCurrUserBlacklist(String token,Long remainingTime);

    public boolean isInBlacklist(String token);
}
