package com.liu.file.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.liu.common.exception.BusinessException;
import com.liu.common.untils.UserContext;
import com.liu.file.Service.IKnowledgeCacheService;
import com.liu.file.domain.Entity.Knowledge;
import com.liu.file.domain.VO.KnowledgeVO;
import com.liu.file.mapper.MindKnowledgeMapper;
import com.liu.file.Bloom.KnowledgeBloomFilterManager;
import com.liu.common.aop.CacheMonitor;
import com.liu.common.common.constant.RedisConstant;
import com.liu.common.config.redisConfig.StringRedisTemplateConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
        Long userId = UserContext.getUserId();
        String key = RedisConstant.KNOWLEDGE_ID + userId + "_" + id;



        //1.先查询布隆过滤器是否存在数据
        boolean contain = knowledgeBloom.isKnowledgeContain(id);
        if(!contain){
            return null;
        }

        //2.查询本地缓存的数据
        KnowledgeVO localVo = knowledgeVOLocalCache.getIfPresent(id.toString());
        if(localVo!=null){
            log.info("命中本地缓存，直接返回");
            return localVo;
        }

        //3.redis的查询
        boolean locked = false;
        String lockKey = "KnowledgeIds:" + id + "_" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            KnowledgeVO knowledgeVO = redisCacheUtils.get(key, KnowledgeVO.class);
            if(knowledgeVO != null){
                log.info("命中redis缓存,返回数据");
                knowledgeVOLocalCache.put(id.toString(), knowledgeVO);
                return knowledgeVO;
            }


            KnowledgeVO vo = null;
            //4.数据库查询
            boolean tryLock = lock.tryLock(30, TimeUnit.SECONDS);
            locked = tryLock;
            if(!tryLock){
                log.warn("获取分布式锁失败，知识库ID：{}", id);
                Thread.sleep(100);
                vo = redisCacheUtils.get(key,KnowledgeVO.class);
                if(vo!=null){
                    log.info("其他线程完成写回，redis命中知识库缓存，并写回本地缓存，知识库ID：{}", id);
                    knowledgeVOLocalCache.put(id.toString(),vo);
                    return vo;
                }
                throw new BusinessException("系统繁忙，请稍后重试");
            }

            log.info("获取分布式锁成功，知识库ID：{}", id);
            vo = redisCacheUtils.get(key,KnowledgeVO.class);
            if (vo != null) {
                log.info("双重检查命中Redis缓存，知识库ID：{}", id);
                knowledgeVOLocalCache.put(id.toString(), vo);
                return vo;
            }

            Knowledge know = getById(id);

            //如果不存在缓存空值，返回null;
            if(know == null){
                redisCacheUtils.setEmptyValue(key,RedisConstant.CACHE_NULL_TTL);
                return null;
            }

            KnowledgeVO resultVo = BeanUtil.copyProperties(know, KnowledgeVO.class);
            knowledgeVOLocalCache.put(id.toString(), resultVo);
            redisCacheUtils.setWithRandomExpire(key,resultVo,RedisConstant.KNOWLEDGE_ID_TTL);
            return resultVo;

        } catch (BusinessException e) {
            log.warn("业务处理警告，knowledgeId={}, msg={}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("redis缓存查询失败",e);
            return BeanUtil.copyProperties(getById(id),KnowledgeVO.class);
        }finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 更新时同步更新缓存数据
     * @param knowledge
     */
    @Override
    public void updateKnowledge(Knowledge knowledge) {
        Long userId = UserContext.getUserId();
        String key = RedisConstant.KNOWLEDGE_ID + userId + "_" + knowledge.getId();
        try {
            KnowledgeVO knowledgeVO = BeanUtil.copyProperties(knowledge, KnowledgeVO.class);
            redisCacheUtils.delete(key);
            knowledgeVOLocalCache.invalidate(knowledge.getId().toString());
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
        Long userId = UserContext.getUserId();
        String key = RedisConstant.KNOWLEDGE_ID + userId + "_" + id;
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
            if(knowledgeVO == null){
                continue;
            }
            result.add(knowledgeVO);
        }
        return result;
    }

    @Override
    @CacheMonitor(cacheName = "knowledge")
    public Long knowledgeCountNum() {
        Long userId = UserContext.getUserId();
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
        Long userId = UserContext.getUserId();
        String key = RedisConstant.KNOWLEDGE_COUNT_NUM + userId;
        redisCacheUtils.delete(key);
    }


}
