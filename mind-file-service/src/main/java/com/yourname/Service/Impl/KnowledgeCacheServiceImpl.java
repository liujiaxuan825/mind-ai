package com.yourname.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.yourname.Service.IKnowledgeCacheService;
import com.yourname.domain.Entity.Knowledge;
import com.yourname.domain.VO.KnowledgeVO;
import com.yourname.mapper.MindKnowledgeMapper;
import com.yourname.mind.Bloom.KnowledgeBloomFilterManager;
import com.yourname.mind.aop.CacheMonitor;
import com.yourname.mind.common.constant.RedisConstant;
import com.yourname.mind.config.StringRedisTemplateConfig;
import com.yourname.mind.config.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeCacheServiceImpl extends ServiceImpl<MindKnowledgeMapper, Knowledge> implements IKnowledgeCacheService {
    
    private final StringRedisTemplateConfig.RedisCacheUtils redisCacheUtils;

    private final KnowledgeBloomFilterManager knowledgeBloom;

    private final Cache<String, KnowledgeVO> knowledgeVOLocalCache;

    private final RedissonClient redissonClient;

    /**
     * 获取单个缓存的过程
     * @param id
     * @return
     */
    @Override
    @CacheMonitor(cacheName = "knowledge")
    public KnowledgeVO getKnowledgeById(Long id) {
        Long userId = UserContextHolder.getCurrentUserId();
        String key = RedisConstant.KNOWLEDGE_ID + userId + "_" + id;

        String lockKey = "KnowledgeIds:" + id + userId;
        RLock lock = redissonClient.getLock(lockKey);

        //1.先查询布隆过滤器是否存在数据
        boolean contain = knowledgeBloom.isKnowledgeContain(id);
        if(!contain){
            return null;
        }

        //2.查询本地缓存的数据
        KnowledgeVO localVo = knowledgeVOLocalCache.getIfPresent(key);
        if(localVo!=null){
            log.info("命中本地缓存，直接返回");
            return localVo;
        }

        //3.redis的查询
        try {
            KnowledgeVO knowledgeVO = redisCacheUtils.get(key, KnowledgeVO.class);
            if(knowledgeVO!=null){
                log.info("命中redis缓存,返回数据");
                knowledgeVOLocalCache.put(key, knowledgeVO);
                return knowledgeVO;
            }


            KnowledgeVO vo = null;
            //4.数据库查询
            boolean tryLock = lock.tryLock(5, 30, TimeUnit.SECONDS);
            if(!tryLock){
                log.warn("获取分布式锁失败，知识库ID：{}", id);
                Thread.sleep(100);
                vo = redisCacheUtils.get(key,KnowledgeVO.class);
                if(vo!=null){
                    knowledgeVOLocalCache.put(key,vo);
                    return vo;
                }
            }

            log.info("获取分布式锁成功，知识库ID：{}", id);
            vo = redisCacheUtils.get(key,KnowledgeVO.class);
            if (vo != null) {
                log.info("双重检查命中Redis缓存，知识库ID：{}", id);
                knowledgeVOLocalCache.put(key, vo);
                return vo;
            }
            localVo = knowledgeVOLocalCache.getIfPresent(key);
            if (localVo != null) {
                log.info("双重检查命中本地缓存，知识库ID：{}", id);
                return localVo;
            }


            Knowledge know = getById(id);

            //如果不存在缓存空值，返回null;
            if(know==null){
                redisCacheUtils.setEmptyValue(key,RedisConstant.CACHE_NULL_TTL);
                return null;
            }

            KnowledgeVO resultVo = BeanUtil.copyProperties(know, KnowledgeVO.class);
            knowledgeVOLocalCache.put(key,resultVo);
            redisCacheUtils.setWithRandomExpire(key,resultVo,RedisConstant.KNOWLEDGE_ID_TTL);
            return resultVo;

        } catch (Exception e) {
            log.error("redis缓存失败，{}",e);
            return BeanUtil.copyProperties(getById(id),KnowledgeVO.class);
        }finally {
            lock.unlock();
        }
    }

    /**
     * 更新时同步更新缓存数据
     * @param knowledge
     */
    @Override
    public void updateKnowledge(Knowledge knowledge) {
        Long userId = UserContextHolder.getCurrentUserId();
        String key = RedisConstant.KNOWLEDGE_ID + userId + knowledge.getId();
        try {
            KnowledgeVO knowledgeVO = BeanUtil.copyProperties(knowledge, KnowledgeVO.class);
            redisCacheUtils.setWithRandomExpire(key,knowledgeVO,RedisConstant.KNOWLEDGE_ID_TTL);
        } catch (Exception e) {
            log.error("redis更新缓存失败,{}",e);
        }
    }

    /**
     * 删除数据时直接删除缓存
     * @param id
     */
    @Override
    public void deleteKnowledge(Long id) {
        Long userId = UserContextHolder.getCurrentUserId();
        String key = RedisConstant.KNOWLEDGE_ID + userId + id;
        try {
            redisCacheUtils.delete(key);
        } catch (Exception e) {
            log.error("redis缓存删除失败，{}",e);
        }
    }

    /**
     * 批量获取缓存，为分页查询服务
     * @param ids
     * @return
     */
    @Override
    public List<KnowledgeVO> getKnowledgeList(List<Long> ids) {
        if(CollectionUtil.isEmpty(ids)){
            return Collections.emptyList();
        }
        List<KnowledgeVO> result = new ArrayList<>();
        for (Long id : ids) {
            KnowledgeVO knowledgeVO = getKnowledgeById(id);
            if(knowledgeVO==null){
                continue;
            }
            result.add(knowledgeVO);
        }
        return result;
    }

    @Override
    @CacheMonitor(cacheName = "knowledge")
    public Long knowledgeCountNum() {
        Long userId = UserContextHolder.getCurrentUserId();
        String key = RedisConstant.KNOWLEDGE_COUNT_NUM + userId;
        try {
            Long num = redisCacheUtils.get(key, Long.class);
            if (num!=null){
                return num;
            }
            LambdaQueryWrapper<Knowledge> lqw = new LambdaQueryWrapper<>();
            lqw.eq(Knowledge::getUserId,userId);
            long count = count(lqw);
            redisCacheUtils.setWithKeep(key,count);
            return count;
        } catch (Exception e) {
            log.error("redis缓存查询数量失败,{}",e);
            return count(new LambdaQueryWrapper<Knowledge>().eq(Knowledge::getUserId,userId));
        }
    }

    @Override
    public void deleteKnowledgeCountNum() {
        Long userId = UserContextHolder.getCurrentUserId();
        String key = RedisConstant.KNOWLEDGE_COUNT_NUM + userId;
        redisCacheUtils.delete(key);
    }


}
