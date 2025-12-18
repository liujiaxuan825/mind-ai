package com.yourname.mind.Bloom;

import com.yourname.Service.IMindKnowledgeService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KnowledgeBloomFilterManager {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private IMindKnowledgeService iMindKnowledgeService;

    @Resource
    private RBloomFilter<Long> KnowledgeBloomFilter;

    private static final String knowledgeBloomKey = "mind-ai:bloom:knowledge:id";

    @PostConstruct
    public void initKnowledgeBloomFilter(){
        KnowledgeBloomFilter = redissonClient.getBloomFilter(knowledgeBloomKey);

    }
}
