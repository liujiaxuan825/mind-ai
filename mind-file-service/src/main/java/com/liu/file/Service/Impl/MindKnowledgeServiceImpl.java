package com.liu.file.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.liu.common.untils.AliyunOssUtil;
import com.liu.common.untils.UserContext;
import com.liu.file.Service.IDocumentCacheService;
import com.liu.file.Service.IKnowledgeCacheService;
import com.liu.file.Service.IMindDocumentService;
import com.liu.file.domain.DTO.KnowledgeDTO;
import com.liu.file.domain.Entity.Document;
import com.liu.file.domain.Entity.Knowledge;
import com.liu.file.mapper.MindKnowledgeMapper;
import com.liu.file.Service.IMindKnowledgeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.file.domain.VO.KnowledgeVO;
import com.liu.file.Bloom.KnowledgeBloomFilterManager;
import com.liu.common.common.Result;
import com.liu.common.common.constant.RedisConstant;
import com.liu.common.common.page.PageRequestDTO;
import com.liu.common.common.page.PageResultVO;
import com.liu.common.config.redisConfig.StringRedisTemplateConfig;
import com.liu.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


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

    private final Cache<String, KnowledgeVO> knowledgeVOLocalCache;

    private final AliyunOssUtil aliyunOssUtil;
    @Lazy
    private final KnowledgeBloomFilterManager  knowledgeBloomFilterManager;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addKnowledge(KnowledgeDTO knowledgeDTO) {
        Long userId = UserContext.getUserId();

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
        knowledgeVOLocalCache.put(id.toString(), knowledgeVO);
        knowledgeBloomFilterManager.addKnowledgeToBloom(id);

        redisCacheUtils.setWithRandomExpire(RedisConstant.KNOWLEDGE_ID + userId + "_" + id, knowledgeVO, RedisConstant.KNOWLEDGE_ID_TTL);
    }

    /**
     * @param pageDTO
     * @return
     */
    @Override
    public Result<PageResultVO<KnowledgeVO>> pageSelect(PageRequestDTO pageDTO) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Knowledge> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Knowledge::getUserId, userId)
                .select(Knowledge::getId);
        Page<Knowledge> page = this.page(pageDTO.toMpPage(), lqw);
        List<Long> ids = page.getRecords().stream().map(Knowledge::getId).toList();
        List<KnowledgeVO> knowledgeList = iKnowledgeCacheService.getKnowledgeList(ids);
        PageResultVO<KnowledgeVO> result = PageResultVO.success(knowledgeList, page.getTotal(), pageDTO);
        return Result.success(result);
    }

    @Override
    @Transactional
    public void deleteKnowledge(List<Long> kbId) {
        LambdaQueryWrapper<Knowledge> knowLqw = new LambdaQueryWrapper<>();
        knowLqw.in(Knowledge::getId, kbId).select(Knowledge::getId).select(Knowledge::getCoverUrl);
        List<Knowledge> list = this.list(knowLqw);
        if(list.isEmpty()){
            throw new BusinessException("知识库不存在！");
        }

        //删除知识库集合
        this.removeBatchByIds(kbId);

        //删除知识库redis缓存和本地缓存
        for (Long id : kbId) {
            iKnowledgeCacheService.deleteKnowledge(id);
            knowledgeVOLocalCache.invalidate(id.toString());
        }
        //删除知识库数量缓存
        iKnowledgeCacheService.deleteKnowledgeCountNum();

        //删除阿里云oss中知识库封面的图片
        for (Knowledge knowledge : list) {
            if(knowledge.getCoverUrl() != null){
                String file = knowledge.getCoverUrl().replaceFirst("^https?://.*?\\.aliyuncs\\.com/", "");
                aliyunOssUtil.deleteFile(file);
            }
        }

        //删除与知识库关联的所有文档
        LambdaQueryWrapper<Document> docLqw = new LambdaQueryWrapper<>();
        docLqw.in(Document::getKnowledgeId,kbId);
        mindDocumentService.remove(docLqw);

        //删除文档数量相关缓存
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
