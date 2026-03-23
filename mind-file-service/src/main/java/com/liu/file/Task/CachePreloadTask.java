package com.liu.file.Task;

import com.liu.file.Service.IMindKnowledgeService;
import com.liu.common.mind.config.StringRedisTemplateConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CachePreloadTask {

    private final IMindKnowledgeService mindKnowledgeService;

    private final StringRedisTemplateConfig.RedisCacheUtils redisCacheUtils;

    //TODO: 应该是从记录的日志中访问，热点数据前十

}
