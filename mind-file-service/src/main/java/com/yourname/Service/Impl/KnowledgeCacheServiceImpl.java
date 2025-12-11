package com.yourname.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yourname.Service.IKnowledgeCacheService;
import com.yourname.domain.Entity.Knowledge;
import com.yourname.domain.VO.KnowledgeVO;
import com.yourname.mapper.MindKnowledgeMapper;
import com.yourname.mind.common.constant.RedisConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeCacheServiceImpl extends ServiceImpl<MindKnowledgeMapper, Knowledge> implements IKnowledgeCacheService {
    
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取单个缓存的过程
     * @param id
     * @return
     */
    @Override
    public Knowledge getKnowledgeById(Long id) {
        String key = RedisConstant.KNOWLEDGE_ID+id;
        try {
            String knowledge = stringRedisTemplate.opsForValue().get(key);
            if(!knowledge.isEmpty()){
                return JSONUtil.toBean(knowledge,Knowledge.class);
            }
            Knowledge know = getById(id);
            //如果不存在缓存空值，返回null;
            if(know==null){
                stringRedisTemplate.opsForValue().set(key,RedisConstant.CACHE_NULL_OBJECT,RedisConstant.CACHE_NULL_TTL,TimeUnit.SECONDS);
            }
            String json = JSONUtil.toJsonStr(know);
            stringRedisTemplate.opsForValue().set(key,json,RedisConstant.KNOWLEDGE_ID_TTL, TimeUnit.SECONDS);
            return know;
        } catch (Exception e) {
            log.error("redis缓存失败，{}",e);
            return getById(id);
        }
    }

    /**
     * 更新时同步更新缓存数据
     * @param knowledge
     */
    @Override
    public void updateKnowledge(Knowledge knowledge) {
        String key = RedisConstant.KNOWLEDGE_ID+knowledge.getId();
        try {
            KnowledgeVO knowledgeVO = BeanUtil.copyProperties(knowledge, KnowledgeVO.class);
            String json = JSONUtil.toJsonStr(knowledgeVO);
            stringRedisTemplate.opsForValue().set(key,json,RedisConstant.KNOWLEDGE_ID_TTL,TimeUnit.SECONDS);
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
        String key = RedisConstant.KNOWLEDGE_ID + id;
        try {
            stringRedisTemplate.delete(key);
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
        for (Long id : ids) {
            Knowledge knowledge = getKnowledgeById(id);
            if(knowledge==null){
                continue;
            }

        }
        return null;
    }


}
