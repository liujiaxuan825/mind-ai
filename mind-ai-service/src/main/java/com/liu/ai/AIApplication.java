package com.liu.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.liu.ai")
@MapperScan("com.liu.ai.mapper")
@EnableDiscoveryClient
public class AIApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(AIApplication.class, args);
    }
}
