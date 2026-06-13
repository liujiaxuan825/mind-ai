package com.liu.file.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.liu.common.exception.BusinessException;
import com.liu.common.untils.UserContext;
import com.liu.file.Bloom.DocumentBloomFilterManager;
import com.liu.file.Service.IDocumentCacheService;
import com.liu.file.domain.Entity.Document;
import com.liu.file.domain.VO.DocumentVO;
import com.liu.file.mapper.MindDocumentMapper;
import com.liu.common.aop.CacheMonitor;
import com.liu.common.common.constant.RedisConstant;
import com.liu.common.config.redisConfig.StringRedisTemplateConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentCacheServiceImpl extends ServiceImpl<MindDocumentMapper, Document> implements IDocumentCacheService {

    private final StringRedisTemplateConfig.RedisCacheUtils redisCacheUtils;

    private final Cache<String, DocumentVO> documentCache;

    private final RedissonClient redissonClient;

    private final DocumentBloomFilterManager documentBloomFilterManager;



    @Override
    @CacheMonitor(cacheName = "document")
    public Long countNum() {
        Long userId = UserContext.getUserId();
        String key = RedisConstant.DOCUMENT_COUNT_NUM + userId;
        Long num = redisCacheUtils.get(key, Long.class);

        if(num != null){
            return num;
        }

        LambdaQueryWrapper<Document> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Document::getCreatedByUserId,userId);
        lqw.eq(Document::getIsDeleted,0);
        long count = count(lqw);
        redisCacheUtils.setWithKeep(key,count);
        return count;
    }

    @Override
    public void deleteCountNum() {
        Long userId = UserContext.getUserId();
        String key = RedisConstant.DOCUMENT_COUNT_NUM + userId;
        try {
            redisCacheUtils.delete(key);
        } catch (Exception e) {
            log.error("redis缓存删除数量失败,{}",e);
        }
    }

    @Override
    @CacheMonitor(cacheName = "document")
    public DocumentVO getDocument(Long docId) {
        Long userId = UserContext.getUserId();
        String cacheKey = RedisConstant.DOCUMENT_CACHE_DISABLE + userId + "_" + docId;
        boolean exists = redisCacheUtils.exists(cacheKey);
        if (exists) {
            return getDocumentFromDb(docId);
        }
        if(!documentBloomFilterManager.isDocumentContain(docId)){
            return null;
        }
        // 2. 再判断本地缓存是否存在
        DocumentVO localVO = documentCache.getIfPresent(docId.toString());
        if (localVO != null) {
            log.info("本地缓存命中文档，docId={}", docId);
            return localVO;
        }

        // 3. 再从redis中获取
        boolean locked = false;
        String lockKey = "DocumentIds:" + docId + "_" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            String key = RedisConstant.DOCUMENT_ID + userId + "_" + docId;
            DocumentVO documentVO = redisCacheUtils.get(key, DocumentVO.class);
            if (documentVO != null) {
                log.info("redis命中文档，并写回本地缓存，docId={}", docId);
                documentCache.put(docId.toString(), documentVO);
                return documentVO;
            }

            boolean tryLock = lock.tryLock(30, TimeUnit.SECONDS);
            locked = tryLock;
            DocumentVO vo = null;
            if (!tryLock) {
                log.info("分布式锁获取失败，docId={}", docId);
                Thread.sleep(100);
                vo = redisCacheUtils.get(key, DocumentVO.class);
                if (vo != null) {
                    log.info("其他线程完成写回，redis命中文档，并写回本地缓存，docId={}", docId);
                    documentCache.put(docId.toString(), vo);
                    return vo;
                }
                throw new BusinessException("系统繁忙，请稍后重试");
            }
                log.info("分布式锁获取成功，docId={}", docId);
                vo = redisCacheUtils.get(key, DocumentVO.class);
                if (vo != null) {
                    log.info("双重检查命中Redis缓存，文档ID：{}", docId);
                    documentCache.put(docId.toString(), vo);
                    return vo;
                }
                vo = getDocumentFromDb(docId);
                if (vo == null) {
                    redisCacheUtils.setEmptyValue(key, RedisConstant.CACHE_NULL_TTL);
                    return null;
                }
                documentCache.put(docId.toString(), vo);
                redisCacheUtils.setWithRandomExpire(key, vo, RedisConstant.DOCUMENT_ID_TTL);
                return vo;

        } catch (BusinessException e) {
            log.warn("业务处理警告，docId={}, msg={}", docId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("获取文档失败，docId={}", docId, e);
            return getDocumentFromDb(docId);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public DocumentVO getDocumentFromDb(Long docId) {
        Long userId = UserContext.getUserId();
        Document document = getOne(new  LambdaQueryWrapper<Document>().eq(Document::getId, docId)
        .eq(Document::getCreatedByUserId,userId).eq(Document::getIsDeleted,0));
        if(document==null){
            return null;
        }
        return BeanUtil.copyProperties(document,DocumentVO.class);
    }
}
