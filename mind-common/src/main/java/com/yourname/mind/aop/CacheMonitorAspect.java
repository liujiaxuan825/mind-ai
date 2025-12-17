package com.yourname.mind.aop;

import com.yourname.mind.aop.config.CacheContextHolder;
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

    //避免高并发不停创建对象
    private CacheMonitorBO creatCacheMonitorBO(String cacheName) {
        CacheMonitorBO monitorBO = monitorDataMap.get(cacheName);
        if (monitorBO != null) {
            return monitorBO;
        }
        synchronized (monitorDataMap) {
            monitorBO = new CacheMonitorBO();
            monitorBO.setCacheName(cacheName);
            monitorDataMap.put(cacheName, monitorBO);
        }
        return monitorBO;
    }

    @Pointcut("@annotation(com.yourname.mind.aop.CacheMonitor)")
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
        CacheMonitorBO monitorBO = creatCacheMonitorBO(cacheName);
        long start = System.currentTimeMillis();
        try {

            Object result = joinPoint.proceed();


            Boolean isHit = CacheContextHolder.getCacheHit();
            Boolean isExp = CacheContextHolder.getCacheException();
            if (isHit){
                monitorBO.getHitCount().incrementAndGet();//命中了
            } else {
                monitorBO.getMissCount().incrementAndGet();//未命中
            }
            if (isExp){
                monitorBO.getExceptionCount().incrementAndGet();//异常了
            }
            return result;
        } catch (Throwable e) {
            monitorBO.getExceptionCount().incrementAndGet();
            throw new RuntimeException(e);
        } finally {
            long end = System.currentTimeMillis();
            long costTime = end - start;
            monitorBO.getTotalInvokeCount().incrementAndGet();
            monitorBO.getTotalCostTime().addAndGet(costTime);
            if (costTime > monitorBO.getMaxCostTime().get()) {
                monitorBO.getMaxCostTime().set(costTime);
            }
            if (costTime < monitorBO.getMinCostTime().get()) {
                monitorBO.getMinCostTime().set(costTime);
            }
            CacheContextHolder.clear();
        }

    }
}
