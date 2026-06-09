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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
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

    private final Cache<String, KnowledgeVO> knowledgeVOLocalCache;

    private final AliyunOssUtil aliyunOssUtil;
    @Lazy
    private final KnowledgeBloomFilterManager  knowledgeBloomFilterManager;

    @Autowired
    private MindKnowledgeServiceImpl knowledgeServiceProxy;


    @Override
    public void addKnowledge(KnowledgeDTO knowledgeDTO) {
        if (knowledgeDTO == null) {
            throw new BusinessException("参数不能为空");
        }
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        Knowledge knowledge = new Knowledge();
        BeanUtil.copyProperties(knowledgeDTO, knowledge);
        knowledge.setUserId(userId);

        //存入数据库
        try {
            this.save(knowledge);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("知识库名称不能重复！");
        }

        //删除知识库数量的缓存
        iKnowledgeCacheService.deleteKnowledgeCountNum();

        Long id = knowledge.getId();
        //存入本地缓存， 布隆和redis
        try {
            knowledgeVOLocalCache.put(id.toString(), BeanUtil.copyProperties(knowledge, KnowledgeVO.class));
        } catch (Exception e) {
            log.error("写入本地缓存失败");
        }
        try {
            knowledgeBloomFilterManager.addKnowledgeToBloom(id);
        } catch (Exception e) {
            log.error("布隆过滤器新增ID失败");
        }

        redisCacheUtils.setWithRandomExpire(RedisConstant.KNOWLEDGE_ID + userId + "_" + id, BeanUtil.copyProperties(knowledge, KnowledgeVO.class), RedisConstant.KNOWLEDGE_ID_TTL);
    }

    /**
     * @param pageDTO
     * @return
     */
    @Override
    public Result<PageResultVO<KnowledgeVO>> pageSelect(PageRequestDTO pageDTO) {
        if (pageDTO == null) {
            throw new BusinessException("参数不能为空");
        }
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        LambdaQueryWrapper<Knowledge> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Knowledge::getUserId, userId)
                .select(Knowledge::getId);
        //TODO: 默认mybatis-plus分页查询，后续可以优化
        Page<Knowledge> page = this.page(pageDTO.toMpPage(), lqw);
        List<Long> ids = page.getRecords().stream().map(Knowledge::getId).toList();
        List<KnowledgeVO> knowledgeList = iKnowledgeCacheService.getKnowledgeList(ids);
        PageResultVO<KnowledgeVO> result = PageResultVO.success(knowledgeList, page.getTotal(), pageDTO);
        return Result.success(result);
    }

    @Override
    public void deleteKnowledge(List<Long> kbId) {
        if (kbId == null || kbId.isEmpty()) {
            throw new BusinessException("知识库列表不能为空");
        }
        LambdaQueryWrapper<Knowledge> knowLqw = new LambdaQueryWrapper<>();
        knowLqw.in(Knowledge::getId, kbId).select(Knowledge::getId).select(Knowledge::getCoverUrl);
        List<Knowledge> list = this.list(knowLqw);
        if(list.isEmpty()){
            throw new BusinessException("知识库不存在！");
        }
        List<Long> realDeleteIds = list.stream()
                .map(Knowledge::getId)
                .filter(Objects::nonNull)
                .toList();
        try {
            knowledgeServiceProxy.deleteKnowledgeAndDoc(realDeleteIds);
        } catch (Exception e) {
            log.error("删除知识库失败", e);
            throw new BusinessException("删除知识库失败");
        }
        knowledgeServiceProxy.deleteCacheKnoAndDoc(realDeleteIds, list);
    }

    @Async("commonThreadPool")
    public void deleteCacheKnoAndDoc(List<Long> kbId, List<Knowledge> list){
        //删除知识库redis缓存和本地缓存
        for (Long id : kbId) {
            iKnowledgeCacheService.deleteKnowledge(id);
            try {
                knowledgeVOLocalCache.invalidate(id.toString());
            } catch (Exception e) {
                log.error("删除知识库本地缓存失败", e);
            }
        }
        //删除知识库数量缓存
        iKnowledgeCacheService.deleteKnowledgeCountNum();
        //删除文档数量相关缓存
        iDocumentCacheService.deleteCountNum();
        //删除阿里云oss中知识库封面的图片
        List<String> keys = list.stream()
                .filter(knowledge -> knowledge.getCoverUrl() != null)
                .map(knowledge -> knowledge.getCoverUrl().replaceFirst("^https?://.*?\\.aliyuncs\\.com/", ""))
                .toList();
        try {
            aliyunOssUtil.deleteFiles(keys);
        } catch (Exception e) {
            log.error("删除知识库封面图片失败", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgeAndDoc(List<Long> kbId){

        //删除与知识库关联的所有文档
        LambdaQueryWrapper<Document> docLqw = new LambdaQueryWrapper<>();
        docLqw.in(Document::getKnowledgeId,kbId);
        mindDocumentService.remove(docLqw);

        //删除知识库集合
        this.removeBatchByIds(kbId);
    }

    @Override
    public void updateKnowledge(KnowledgeDTO knowledgeDTO) {
        if(knowledgeDTO == null){
            throw new BusinessException("参数不能为空");
        }
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
