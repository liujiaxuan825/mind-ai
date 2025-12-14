package com.yourname.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yourname.Service.IDocumentCacheService;
import com.yourname.domain.Entity.Document;
import com.yourname.mapper.MindDocumentMapper;
import com.yourname.mind.common.constant.RedisConstant;
import com.yourname.mind.config.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentCacheServiceImpl extends ServiceImpl<MindDocumentMapper, Document> implements IDocumentCacheService {

    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public Long countNum() {
        Long userId = UserContextHolder.getCurrentUserId();
        String key = RedisConstant.DOCUMENT_COUNT_NUM + userId;
        String num = stringRedisTemplate.opsForValue().get(key);

        if(num!=null){
            return Long.valueOf(num);
        }

        LambdaQueryWrapper<Document> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Document::getCreatedByUserId,userId);
        long count = count(lqw);
        stringRedisTemplate.opsForValue().set(key,String.valueOf(count));
        return count;
    }

    @Override
    public void deleteCountNum() {
        Long userId = UserContextHolder.getCurrentUserId();
        String key = RedisConstant.DOCUMENT_COUNT_NUM + userId;
        stringRedisTemplate.delete(key);
    }
}
