package com.yourname.mind.aop.config;

import com.yourname.mind.aop.CacheMonitorBO;

public class CacheContextHolder {

    private static final ThreadLocal<Boolean> CACHE_HIT = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> CACHE_NULL = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> CACHE_EXCEPTION = new ThreadLocal<>();

    public static void setCacheHit(Boolean cacheHit) {
        CACHE_HIT.set(cacheHit);
    }

    public static Boolean getCacheHit() {
        return CACHE_HIT.get();
    }

    public static void setCacheException(Boolean exception) {
        CACHE_EXCEPTION.set(exception);
    }

    public static Boolean getCacheException() {
        return CACHE_EXCEPTION.get();
    }

    public static void setCacheNull(Boolean cacheNull) {
        CACHE_NULL.set(cacheNull);
    }

    public static Boolean getCacheNull() {
        return CACHE_NULL.get();
    }

    public static void clear() {
        CACHE_HIT.remove();
        CACHE_NULL.remove();
        CACHE_EXCEPTION.remove();
    }
}
