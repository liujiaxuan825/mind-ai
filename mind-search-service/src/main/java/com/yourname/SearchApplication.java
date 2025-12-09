package com.yourname;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 *
 */
@SpringBootApplication(scanBasePackages = "com.yourname")
@MapperScan("com.yourname.mapper")
public class SearchApplication
{
    public static void main( String[] args ) {SpringApplication.run(SearchApplication.class, args);}
}
