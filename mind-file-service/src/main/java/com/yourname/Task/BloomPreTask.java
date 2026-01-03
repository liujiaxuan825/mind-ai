package com.yourname.Task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yourname.Service.IMindKnowledgeService;
import com.yourname.domain.Entity.Knowledge;
import com.yourname.mind.Bloom.KnowledgeBloomFilterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class BloomPreTask {

    private final KnowledgeBloomFilterManager  knowledgeBloomFilterManager;

    private final IMindKnowledgeService  mindKnowledgeService;

    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 0 2 * * ?")
    public void BloomPre() {
        RLock lock = redissonClient.getLock("bloom_pre_lock");
        try {
            boolean success = lock.tryLock(10, 600, TimeUnit.SECONDS);
            if (success) {
                log.info("开始重建知识库布隆过滤器");
                LambdaQueryWrapper<Knowledge> lqw = new LambdaQueryWrapper<>();
                lqw.select(Knowledge::getId);
                List<Knowledge> list = mindKnowledgeService.list(lqw);
                List<Long> data = list.stream().map(Knowledge::getId).toList();
                knowledgeBloomFilterManager.reBuildBloom(data);
                log.info("重建知识库布隆过滤器完成");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
