package com.yourname.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.yourname.Service.IDocumentCacheService;
import com.yourname.Service.IKnowledgeCacheService;
import com.yourname.Service.IMindDocumentService;
import com.yourname.domain.DTO.KnowledgeDTO;
import com.yourname.domain.Entity.Document;
import com.yourname.domain.Entity.Knowledge;
import com.yourname.localCacheConfig.KnowledgeLocalCacheConfig;
import com.yourname.mapper.MindKnowledgeMapper;
import com.yourname.Service.IMindKnowledgeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yourname.domain.VO.KnowledgeVO;
import com.yourname.mind.Bloom.KnowledgeBloomFilterManager;
import com.yourname.mind.common.Result;
import com.yourname.mind.common.constant.RedisConstant;
import com.yourname.mind.common.page.PageRequestDTO;
import com.yourname.mind.common.page.PageResultVO;
import com.yourname.mind.config.StringRedisTemplateConfig;
import com.yourname.mind.config.UserContextHolder;
import com.yourname.mind.exception.BusinessException;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * <p>
 *  知识库服务实现类
 * </p>
 *
 * @author liujiaxuan
 * @since 2025-11-18
 */
@Service
@RequiredArgsConstructor
public class MindKnowledgeServiceImpl extends ServiceImpl<MindKnowledgeMapper, Knowledge> implements IMindKnowledgeService {

    private final IMindDocumentService  mindDocumentService;

    private final IKnowledgeCacheService iKnowledgeCacheService;

    private final IDocumentCacheService iDocumentCacheService;

    private final StringRedisTemplateConfig.RedisCacheUtils redisCacheUtils;

    private final Cache<Long, KnowledgeVO> knowledgeVOLocalCache;

    private final KnowledgeBloomFilterManager  knowledgeBloomFilterManager;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addKnowledge(KnowledgeDTO knowledgeDTO) {
        Long userId = UserContextHolder.getCurrentUserId();

        //判断知识库的唯一
        LambdaQueryWrapper<Knowledge> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Knowledge::getUserId, userId)
                .eq(Knowledge::getName, knowledgeDTO.getName());
        List<Knowledge> list = list(queryWrapper);
        if(!list.isEmpty()){
            throw new BusinessException("知识库名称不能重复");
        }

        Knowledge knowledge = new Knowledge();
        BeanUtils.copyProperties(knowledgeDTO, knowledge);
        knowledge.setUserId(userId);

        //删除知识库数量的缓存
        iKnowledgeCacheService.deleteKnowledgeCountNum();

        //存入数据库
        this.save(knowledge);
        LambdaQueryWrapper<Knowledge> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Knowledge::getUserId, userId)
                .eq(Knowledge::getName, knowledgeDTO.getName());
        Knowledge know = getOne(lqw);

        KnowledgeVO knowledgeVO = BeanUtil.copyProperties(know, KnowledgeVO.class);
        Long id = know.getId();
        //存入本地缓存， 布隆和redis
        knowledgeVOLocalCache.put(id, knowledgeVO);
        knowledgeBloomFilterManager.addKnowledgeToBloom(id);

        redisCacheUtils.setWithRandomExpire(RedisConstant.KNOWLEDGE_ID + userId + "_" + id, knowledgeVO, RedisConstant.KNOWLEDGE_ID_TTL);
    }


    /**
     * 这个分页的缓存是直接将某一页的缓存直接存入redis中
     * @param pageDTO
     * @return
     */
    @Override
    public Result<PageResultVO<KnowledgeVO>> pageSelect(PageRequestDTO pageDTO) {
        Long userId = UserContextHolder.getCurrentUserId();
        String key = RedisConstant.KNOWLEDGE_PAGE + userId + pageDTO.getPageNum() + pageDTO.getPageSize();

        //只能取出集合
        List<KnowledgeVO> voList = redisCacheUtils.get(key, new TypeReference<List<KnowledgeVO>>() {});
        if(voList!=null&& !voList.isEmpty()){
            long total = voList.size();
            PageResultVO<KnowledgeVO> result = PageResultVO.success(voList, total, pageDTO);
            return Result.success(result);
        }


        Page<Knowledge> page = this.page(pageDTO.toMpPage());
        List<Long> ids = page.getRecords().stream().map(Knowledge::getId).toList();
        List<KnowledgeVO> knowledgeList = iKnowledgeCacheService.getKnowledgeList(ids);
        PageResultVO<KnowledgeVO> result = PageResultVO.success(knowledgeList, page.getTotal(), pageDTO);

        //缓存此次分页结果,之缓存集合
        redisCacheUtils.setWithRandomExpire(key, knowledgeList, RedisConstant.KNOWLEDGE_PAGE_TTL);
        return Result.success(result);
    }


    @Override
    @Transactional
    public void deleteKnowledge(List<Long> kbId) {
        LambdaQueryWrapper<Knowledge> knowLqw = new LambdaQueryWrapper<>();
        knowLqw.in(Knowledge::getId, kbId);
        List<Knowledge> list = this.list(knowLqw);
        if(list.isEmpty()){
            throw new BusinessException("知识库不存在！");
        }

        //删除知识库集合
        this.removeBatchByIds(kbId);

        //删除知识库redis缓存和本地缓存
        for (Long id : kbId) {
            iKnowledgeCacheService.deleteKnowledge(id);
            knowledgeVOLocalCache.invalidate(id);
        }
        //删除知识库数量缓存
        iKnowledgeCacheService.deleteKnowledgeCountNum();


        //删除与知识库关联的所有文档
        LambdaQueryWrapper<Document> docLqw = new LambdaQueryWrapper<>();
        docLqw.in(Document::getKnowledgeId,kbId);
        mindDocumentService.remove(docLqw);
        //TODO: 文档的redis缓存和本地缓存的删除

        //删除文档相关缓存
        iDocumentCacheService.deleteCountNum();
    }


    @Override
    @Transactional
    public void updateKnowledge(KnowledgeDTO knowledgeDTO) {
        Knowledge knowledge = BeanUtil.copyProperties(knowledgeDTO, Knowledge.class);
        boolean success = updateById(knowledge);
        if(success){
            iKnowledgeCacheService.updateKnowledge(knowledge);
        }
    }


    @Override
    public Result<Long> countKnowledgeNum() {
        Long countNum = iKnowledgeCacheService.knowledgeCountNum();
        return Result.success(countNum);
    }

}
