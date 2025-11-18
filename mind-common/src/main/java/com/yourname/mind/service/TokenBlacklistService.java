package com.yourname.mind.service;

public interface TokenBlacklistService {
    public void addCurrUserBlacklist(String token,Long remainingTime);

    public boolean isInBlacklist(String token);
}
