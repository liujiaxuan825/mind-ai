package com.liu.common.aop;

import com.liu.common.aop.config.CacheContextHolder;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class CacheMonitorAspect {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, CacheMetrics> metricsMap = new ConcurrentHashMap<>();

    public CacheMonitorAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    private static class CacheMetrics {
        Counter hitCounter;
        Counter missCounter;
        Counter exceptionCounter;
        Counter totalCounter;
        Timer timer;

        CacheMetrics(MeterRegistry registry, String cacheName) {
            this.hitCounter = Counter.builder("cache_hit_total")
                    .tag("cache_name", cacheName)
                    .description("缓存命中次数")
                    .register(registry);
            this.missCounter = Counter.builder("cache_miss_total")
                    .tag("cache_name", cacheName)
                    .description("缓存未命中次数")
                    .register(registry);
            this.exceptionCounter = Counter.builder("cache_exception_total")
                    .tag("cache_name", cacheName)
                    .description("缓存异常次数")
                    .register(registry);
            this.totalCounter = Counter.builder("cache_invoke_total")
                    .tag("cache_name", cacheName)
                    .description("缓存调用总次数")
                    .register(registry);
            this.timer = Timer.builder("cache_cost_time")
                    .tag("cache_name", cacheName)
                    .description("缓存调用耗时")
                    .register(registry);
        }
    }

    private CacheMetrics getOrCreateMetrics(String cacheName) {
        return metricsMap.computeIfAbsent(cacheName, name -> new CacheMetrics(meterRegistry, name));
    }

    @Pointcut("@annotation(com.liu.common.aop.CacheMonitor)")
    public void pointcut() {}

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        CacheContextHolder.clear();

        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        CacheMonitor annotation = method.getAnnotation(CacheMonitor.class);
        boolean enabled = annotation.enabled();
        if (!enabled) {
            return joinPoint.proceed();
        }

        String cacheName = annotation.cacheName();
        CacheMetrics metrics = getOrCreateMetrics(cacheName);
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            Boolean isHit = CacheContextHolder.getCacheHit();
            Boolean isExp = CacheContextHolder.getCacheException();

            metrics.totalCounter.increment();
            if (Boolean.TRUE.equals(isHit)) {
                metrics.hitCounter.increment();
            } else {
                metrics.missCounter.increment();
            }
            if (Boolean.TRUE.equals(isExp)) {
                metrics.exceptionCounter.increment();
            }

            return result;
        } catch (Throwable e) {
            metrics.exceptionCounter.increment();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - start;
            metrics.timer.record(costTime, TimeUnit.MILLISECONDS);
            CacheContextHolder.clear();
        }
    }
}