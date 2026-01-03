package com.yourname.mind.Bloom;

import com.google.common.hash.BloomFilter;
import com.yourname.mind.infer.BloomDataProvider;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Component
public class DocumentBloomFilterManager {

    @Resource(name = "bloomData")
    private BloomDataProvider<Long>  bloomDataProvider;

    @Resource
    private RedissonClient redissonClient;

    private RBloomFilter<Long> DocumentBloomFilter;

    private static final String documentBloomKey = "mind-ai:bloom:document:id";

    private volatile boolean docWarmUpCompleted = false;

    @PostConstruct
    public void initKnowledgeBloomFilter(){
        DocumentBloomFilter = redissonClient.getBloomFilter(documentBloomKey);
        boolean init = DocumentBloomFilter.tryInit(1000, 0.01);
        if(init){
            log.info("知识库布隆过滤器已经准备好啦！");
        }else {
            log.info("知识库布隆过滤器已经存在！复用已有配置");
        }
    }

    public boolean isDocumentContain(Long id){
        if(id == null){
            return false;
        }
        return DocumentBloomFilter.contains(id);
    }

    //添加逻辑
    public void addKnowledgeToBloom(Long id){
        DocumentBloomFilter.add(id);
    }

    public void addAllDocumentToBloom(){
        if(bloomDataProvider == null || docWarmUpCompleted){
            log.info("数据已存入或bean为空，无需执行");
            return ;
        }
        try {
            List<Long> allDocIds = bloomDataProvider.getAllKnowIds();
            if(allDocIds.isEmpty()){
                docWarmUpCompleted = true;
                log.info("数据为空，无需添加数据");
            }


            DocumentBloomFilter.add(allDocIds);
            docWarmUpCompleted = true;
            log.info("成功将指定bloomData Bean的{}条数据存入布隆过滤器", allDocIds.size());
        } catch (Exception e) {
            log.error("将指定bloomData Bean的数据存入布隆过滤器失败", e);
        }
    }
}
