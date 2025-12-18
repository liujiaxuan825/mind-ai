package com.yourname.mind.aliyun;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class OssConfig {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @PostConstruct
    public void init() {
        log.info("=== OSS配置调试信息 ===");
        log.info("endpoint: {}", endpoint);
        log.info("accessKeyId: {}", accessKeyId != null && !accessKeyId.isEmpty() ? "***已配置***" : "NULL");
        log.info("accessKeySecret: {}", accessKeySecret != null && !accessKeySecret.isEmpty() ? "***已配置***" : "NULL");
        log.info("环境变量OSS_ACCESS_KEY_ID: {}", System.getenv("OSS_ACCESS_KEY_ID") != null ? "已设置" : "未设置");
        log.info("环境变量OSS_ACCESS_KEY_SECRET: {}", System.getenv("OSS_ACCESS_KEY_SECRET") != null ? "已设置" : "未设置");
        log.info("=== OSS配置调试结束 ===");
    }

    @Bean
    public OSS ossClient() {
        log.info("创建OSS客户端，endpoint: {}", endpoint);
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}