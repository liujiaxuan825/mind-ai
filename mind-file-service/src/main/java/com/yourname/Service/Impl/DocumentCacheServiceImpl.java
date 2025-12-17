package com.yourname.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yourname.Service.IDocumentCacheService;
import com.yourname.domain.Entity.Document;
import com.yourname.mapper.MindDocumentMapper;
import com.yourname.mind.aop.CacheMonitor;
import com.yourname.mind.common.constant.RedisConstant;
import com.yourname.mind.config.StringRedisTemplateConfig;
import com.yourname.mind.config.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentCacheServiceImpl extends ServiceImpl<MindDocumentMapper, Document> implements IDocumentCacheService {

    private final StringRedisTemplateConfig.RedisCacheUtils redisCacheUtils;


    @Override
    @CacheMonitor(cacheName = "document")
    public Long countNum() {
        Long userId = UserContextHolder.getCurrentUserId();
        String key = RedisConstant.DOCUMENT_COUNT_NUM + userId;
        Long num = redisCacheUtils.get(key, Long.class);

        if(num!=null){
            return num;
        }

        LambdaQueryWrapper<Document> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Document::getCreatedByUserId,userId);
        long count = count(lqw);
        redisCacheUtils.setWithKeep(key,count);
        return count;
    }

    @Override
    public void deleteCountNum() {
        Long userId = UserContextHolder.getCurrentUserId();
        String key = RedisConstant.DOCUMENT_COUNT_NUM + userId;
        redisCacheUtils.delete(key);
    }
}
