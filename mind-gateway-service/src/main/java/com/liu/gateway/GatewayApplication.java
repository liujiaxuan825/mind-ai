package com.liu.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;


@SpringBootApplication(scanBasePackages = {"com.liu.gateway"})
@ConfigurationPropertiesScan
@EnableDiscoveryClient
@RefreshScope
public class GatewayApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(GatewayApplication.class, args);
    }
}