/*
package com.liu.common.config.retryConfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

@Slf4j
public abstract class AbstractGenericRetryListen <T> implements RetryListener {

    @Value("${spring.rabbitmq.listener.retry.max-attempts:3}")
    private int maxAttempts;

    @Override
    public <S, E extends Throwable> boolean open(RetryContext context, RetryCallback<S, E> callback) {
        return true; // 允许重试
    }

    @Override
    public <S, E extends Throwable> void onError(RetryContext context, RetryCallback<S, E> callback, Throwable throwable) {
        // 从上下文获取消息体
        T message = getMessage(context);
        if (message == null) {
            log.warn("重试失败，但无法获取消息体，重试次数: {}", context.getRetryCount());
            return;
        }

        int retryCount = context.getRetryCount();
        // 调用业务实现的单次失败处理
        onSingleRetryFailed(message, retryCount, throwable);

        // 最后一次重试失败（重试耗尽）
        if (retryCount == maxAttempts) {
            log.error("消息重试次数耗尽，将进入死信队列");
            // 调用业务实现的重试耗尽处理
            onRetryExhausted(message, throwable);
        }
    }

    */
/**
     * 重试结束后调用（无论成功失败）
     *//*

    @Override
    public <S, E extends Throwable> void close(RetryContext context, RetryCallback<S, E> callback, Throwable throwable) {
        // 可在这里添加统一的监控统计逻辑
    }

    */
/**
     * 从重试上下文获取消息体
     *//*

    @SuppressWarnings("unchecked")
    private T getMessage(RetryContext context) {
        return (T) context.getAttribute(MessageContextInterceptor.MESSAGE_CONTEXT_KEY);
    }

    // ==================== 业务层需要实现的抽象方法 ====================

    */
/**
     * 单次重试失败时的业务处理
     * @param message 消息体
     * @param retryCount 当前重试次数（从0开始）
     * @param throwable 异常信息
     *//*

    protected abstract void onSingleRetryFailed(T message, int retryCount, Throwable throwable);

    */
/**
     * 所有重试耗尽时的业务处理
     * @param message 消息体
     * @param throwable 最后一次异常信息
     *//*

    protected abstract void onRetryExhausted(T message, Throwable throwable);

}
*/
