/*
package com.liu.common.config.retryConfig;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Component
public class MessageContextInterceptor implements MethodInterceptor {

    public static final String MESSAGE_CONTEXT_KEY = "retryContextMessage";

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        Object[] arguments = invocation.getArguments();
        if (arguments.length > 0 && arguments[0] != null) {
            RetryContext retryContext = RetrySynchronizationManager.getContext();
            if (retryContext != null) {
                retryContext.setAttribute(MESSAGE_CONTEXT_KEY, arguments[0]);
            }
        }
        return invocation.proceed();
    }
}
*/
