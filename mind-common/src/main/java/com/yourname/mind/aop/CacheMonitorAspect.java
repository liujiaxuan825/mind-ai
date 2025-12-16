package com.yourname.mind.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class CacheMonitorAspect {

    private final Map<String, CacheMonitorBO> monitorDataMap = new ConcurrentHashMap<>();

    @Pointcut("@annotation(com.yourname.mind.aop.CacheMonitor)")
    public void pointcut() {}

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        CacheMonitor annotation = method.getAnnotation(CacheMonitor.class);
        boolean enabled = annotation.enabled();
        if (!enabled) {
            return joinPoint.proceed();
        }
        String cacheName = annotation.cacheName();
        String module = annotation.module();
        CacheMonitorBO monitorBO = monitorDataMap.computeIfAbsent(cacheName, k -> {
            CacheMonitorBO cacheMonitorBO = new CacheMonitorBO();
            cacheMonitorBO.setCacheName(cacheName);
            cacheMonitorBO.setModule(module);
            return cacheMonitorBO;
        });

        return null;
    }
}
