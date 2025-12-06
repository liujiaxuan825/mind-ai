package com.yourname.mind.config;
import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TikaConfig {
    
    /**
     * 配置 Tika 文档解析器
     */
    @Bean
    public Tika tika() {
        return new Tika();
    }
}