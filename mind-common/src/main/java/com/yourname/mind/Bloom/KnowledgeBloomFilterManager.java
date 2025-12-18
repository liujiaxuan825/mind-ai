package com.yourname.mind.Bloom;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yourname.Service.IMindKnowledgeService;
import com.yourname.domain.Entity.Knowledge;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
        boolean init = KnowledgeBloomFilter.tryInit(1000, 0.01);
        if(init){
            log.info("知识库布隆过滤器已经准备好啦！");
        }else {
            log.info("知识库布隆过滤器已经存在！复用已有配置");
        }
    }

    //加载数据，加载到布隆


    //核心判断逻辑
    public boolean isKnowledgeContain(Long id){
        if(id == null){
            return false;
        }
        return KnowledgeBloomFilter.contains(id);
    }

    //添加逻辑
    public void addKnowledgeToBloom(Long id){
        KnowledgeBloomFilter.add(id);
    }
}
