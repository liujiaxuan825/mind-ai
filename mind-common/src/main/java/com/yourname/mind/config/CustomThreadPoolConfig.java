package com.yourname.mind.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 企业级线程池配置类（核心：手动构建ThreadPoolExecutor，拒绝Executors）
 * 作用：将线程池注册为Spring单例Bean，全项目复用
 */
@Configuration
@Slf4j
public class CustomThreadPoolConfig {

    // ========== 第一步：读取布隆预热线程池的配置参数 ==========
    @Value("${thread.pool.bloom.core-size}")
    private int bloomCoreSize;
    @Value("${thread.pool.bloom.max-size}")
    private int bloomMaxSize;
    @Value("${thread.pool.bloom.queue-size}")
    private int bloomQueueSize;
    @Value("${thread.pool.bloom.keep-alive-seconds}")
    private long bloomKeepAlive;
    @Value("${thread.pool.bloom.thread-name-prefix}")
    private String bloomThreadPrefix;

    // ========== 第二步：通用构建方法（避免重复代码，企业级规范） ==========
    /**
     * 线程池通用构建模板
     * @param coreSize 核心线程数
     * @param maxSize 最大线程数
     * @param keepAlive 空闲存活时间（秒）
     * @param queueSize 队列容量
     * @param threadNamePrefix 线程名前缀
     * @return 符合企业级规范的线程池
     */
    private ThreadPoolExecutor buildThreadPool(
            int coreSize, int maxSize, long keepAlive, int queueSize, String threadNamePrefix) {

        // 1. 自定义线程工厂（企业级必备：命名+异常处理）
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                // 线程名：bloom-warmup-0、bloom-warmup-1...（日志中能定位问题）
                .setNameFormat(threadNamePrefix + "%d")
                // 线程优先级（默认即可，不用改）
                .setPriority(Thread.NORM_PRIORITY)
                // 全局异常捕获（关键：线程执行任务抛异常时，不会静默崩溃）
                .setUncaughtExceptionHandler((thread, e) -> {
                    log.error("线程{}执行任务时抛出异常", thread.getName(), e);
                })
                .build();

        // 2. 有界任务队列（企业级必须：避免OOM，绝对不能用无界队列）
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(queueSize);

        // 3. 拒绝策略（企业级兜底：队列满+工人满时，调用者线程执行任务）
        RejectedExecutionHandler rejectHandler = new ThreadPoolExecutor.CallerRunsPolicy();

        // 4. 构建核心线程池（ThreadPoolExecutor是所有线程池的底层实现，企业级唯一推荐）
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                coreSize,        // 核心线程数
                maxSize,         // 最大线程数
                keepAlive,       // 空闲存活时间
                TimeUnit.SECONDS,// 时间单位（秒）
                workQueue,       // 有界任务队列
                threadFactory,   // 自定义线程工厂
                rejectHandler    // 拒绝策略
        );

        // 5. 企业级优化：允许核心线程超时销毁（低峰期释放资源）
        threadPool.allowCoreThreadTimeOut(true);

        // 6. 优雅关闭线程池（企业级必备：JVM退出时，等待任务执行完再关闭）
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM即将关闭，开始优雅关闭线程池：{}", threadNamePrefix);
            threadPool.shutdown(); // 拒绝接收新任务
            try {
                // 等待5分钟，让已提交的任务执行完
                if (!threadPool.awaitTermination(5, TimeUnit.MINUTES)) {
                    log.warn("线程池{}关闭超时，强制中断未完成任务", threadNamePrefix);
                    threadPool.shutdownNow(); // 强制关闭
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow(); // 捕获异常，强制关闭
            }
            log.info("线程池{}已成功关闭", threadNamePrefix);
        }));

        return threadPool;
    }

    // ========== 第三步：注册布隆预热专用线程池（Spring单例Bean） ==========
    /**
     * 布隆过滤器预热专用线程池（Bean名称：bloomWarmupThreadPool）
     * 作用：供全项目注入使用，处理布隆预热的异步任务
     */
    @Bean(name = "bloomWarmupThreadPool") // Bean名称，注入时要匹配
    public ThreadPoolExecutor bloomWarmupThreadPool() {
        // 调用通用方法，传入布隆预热的参数
        return buildThreadPool(
                bloomCoreSize, bloomMaxSize, bloomKeepAlive, bloomQueueSize, bloomThreadPrefix
        );
    }

    // 可扩展：注册MQ消费专用线程池（同理，按你的yml参数传入即可）
    // @Bean(name = "mqConsumeThreadPool")
    // public ThreadPoolExecutor mqConsumeThreadPool() {
    //     return buildThreadPool(mqCoreSize, mqMaxSize, mqKeepAlive, mqQueueSize, mqThreadPrefix);
    // }
}