package com.yourname.localCacheConfig;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yourname.domain.Entity.Knowledge;
import com.yourname.domain.VO.KnowledgeVO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class KnowledgeLocalCacheConfig {

    @Bean
    public Cache<Long, KnowledgeVO> knowledgeVOLocalCache(){
        return Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats()
                .build();
    }
}
