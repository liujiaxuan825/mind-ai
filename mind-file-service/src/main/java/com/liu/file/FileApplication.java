package com.liu.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 *
 *
 */
@SpringBootApplication(scanBasePackages = {"com.liu.file", "com.liu.common.mind", "com.liu.upload"},
    exclude = {MybatisPlusAutoConfiguration.class})
@MapperScan("com.liu.file.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class FileApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }
}