package com.yourname;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.yourname")
@MapperScan("com.yourname.mapper")
public class AIApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(AIApplication.class, args);
    }
}
