package com.yourname.mind.config;

import com.yourname.mind.infer.BloomDataProvider;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Value("${thread.pool.bloom.core-size}")
    private int bloomCoreSize;
    @Value("${thread.pool.bloom.max-size}")
    private int bloomMaxSize;
    @Value("${thread.pool.bloom.queue-size}")
    private int bloomQueueSize;
    @Value("${thread.pool.bloom.keep-alive-seconds}")
    private int bloomKeepAlive;
    @Value("${thread.pool.bloom.thread-name-prefix}")
    private String bloomThreadPrefix;
    @Value("${thread.pool.bloom.businessAwaitTerminationSeconds}")
    private int businessAwaitTerminationSeconds;

    @Bean("bloom")
    public Executor bloomExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(bloomCoreSize);
        executor.setMaxPoolSize(bloomMaxSize);
        executor.setQueueCapacity(bloomQueueSize);
        executor.setThreadNamePrefix(bloomThreadPrefix);
        executor.setKeepAliveSeconds(bloomKeepAlive);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(businessAwaitTerminationSeconds);
        executor.initialize();
        return executor;
    }
}
