package com.yourname.mind.aop;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class CacheMonitorBO {

    private String cacheName;

    private String module;

    private AtomicInteger totalInvokeCount = new AtomicInteger(0);//缓存调用总次数
    private AtomicInteger hitCount = new AtomicInteger(0);//缓存命中总次数
    private AtomicInteger missCount = new AtomicInteger(0);//缓存未命中总次数
    private AtomicInteger hitNullCount = new AtomicInteger(0);//缓存命中空值次数
    private AtomicInteger exceptionCount = new AtomicInteger(0);//缓存异常次数
    private AtomicLong totalCostTime = new AtomicLong(0);//调用所有该缓存总耗时
    private AtomicLong maxCostTime = new AtomicLong(0);//单次调用该缓存的最大耗时
    private AtomicLong minCostTime = new AtomicLong(Long.MAX_VALUE);//单词调用该缓存最短耗时

    public double getHitRate() {
        int total = totalInvokeCount.get();
        return total == 0 ? 0 : (double) hitCount.get()+ (double) hitNullCount.get() / total * 100;
    }

    public long getAvgCostTime() {
        int total = totalInvokeCount.get();
        return total == 0 ? 0 : totalCostTime.get() / total;
    }

}
