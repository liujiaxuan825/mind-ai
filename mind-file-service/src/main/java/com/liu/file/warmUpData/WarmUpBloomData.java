package com.liu.file.warmUpData;

import com.liu.file.Bloom.DocumentBloomFilterManager;
import com.liu.file.Bloom.KnowledgeBloomFilterManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WarmUpBloomData implements ApplicationRunner {

    @Resource
    @Lazy
    private KnowledgeBloomFilterManager knowledgeBloomFilterManager;

    @Resource
    @Lazy
    private DocumentBloomFilterManager documentBloomFilterManager;

    @Override
    public void run(ApplicationArguments args) {
        log.info("初始化布隆过滤器的数据......");
        knowledgeBloomFilterManager.addAllKnowledgeToBloom();
        documentBloomFilterManager.addAllDocumentToBloom();
    }
}
