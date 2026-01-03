package com.yourname.mind.warmUpData;

import com.yourname.mind.Bloom.DocumentBloomFilterManager;
import com.yourname.mind.Bloom.KnowledgeBloomFilterManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WarmUpBloomData implements ApplicationRunner {

    @Resource
    private KnowledgeBloomFilterManager knowledgeBloomFilterManager;

    @Resource
    private DocumentBloomFilterManager documentBloomFilterManager;

    @Override
    public void run(ApplicationArguments args) {
        log.info("初始化布隆过滤器的数据......");
        knowledgeBloomFilterManager.addAllKnowledgeToBloom();
        documentBloomFilterManager.addAllDocumentToBloom();
    }
}
