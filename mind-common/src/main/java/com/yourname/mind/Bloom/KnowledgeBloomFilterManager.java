package com.yourname.mind.Bloom;

import com.yourname.mind.config.ThreadPoolConfig;
import com.yourname.mind.infer.BloomDataProvider;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class KnowledgeBloomFilterManager {

    @Resource(name = "bloomData")
    private BloomDataProvider<Long> bloomDataProvider;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RBloomFilter<Long> KnowledgeBloomFilter;

    private static final String knowledgeBloomKey = "mind-ai:bloom:knowledge:id";

    private volatile boolean warmUpCompleted = false;

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

    @Async("bloom")
    public void addAllKnowledgeToBloom() {
        if(bloomDataProvider==null||warmUpCompleted){
            log.info("数据已存入或bean为空，无需执行");
            return;
        }

        try {
            List<Long> allKnowIds = bloomDataProvider.getAllKnowIds();
            if(allKnowIds.isEmpty()){
                warmUpCompleted = true;
                log.info("数据为空，无需添加数据");
            }

            for (Long id : allKnowIds) {
                addKnowledgeToBloom(id);
            }
            warmUpCompleted = true;
            log.info("成功将指定bloomData Bean的{}条数据存入布隆过滤器", allKnowIds.size());
        } catch (Exception e) {
            log.error("将指定bloomData Bean的数据存入布隆过滤器失败", e);
        }
    }
}
