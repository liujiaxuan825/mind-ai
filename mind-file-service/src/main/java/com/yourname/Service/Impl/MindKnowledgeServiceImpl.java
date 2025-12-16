package com.yourname.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.Service.IDocumentCacheService;
import com.yourname.Service.IKnowledgeCacheService;
import com.yourname.Service.IMindDocumentService;
import com.yourname.domain.DTO.KnowledgeDTO;
import com.yourname.domain.Entity.Document;
import com.yourname.domain.Entity.Knowledge;
import com.yourname.mapper.MindKnowledgeMapper;
import com.yourname.Service.IMindKnowledgeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yourname.domain.VO.KnowledgeVO;
import com.yourname.mind.common.Result;
import com.yourname.mind.common.constant.RedisConstant;
import com.yourname.mind.common.page.PageRequestDTO;
import com.yourname.mind.common.page.PageResultVO;
import com.yourname.mind.config.StringRedisTemplateConfig;
import com.yourname.mind.config.UserContextHolder;
import com.yourname.mind.exception.BusinessException;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


/**
 * <p>
 *  服务实现类
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


    @Override
    public void addKnowledge(KnowledgeDTO knowledgeDTO) {
        Long userId = UserContextHolder.getCurrentUserId();
        Knowledge knowledge = new Knowledge();
        BeanUtils.copyProperties(knowledgeDTO, knowledge);
        knowledge.setUserId(userId);

        //删除知识库数量的缓存
        iKnowledgeCacheService.deleteKnowledgeCountNum();

        this.save(knowledge);
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
        //删除与知识库关联的所有文档
        LambdaQueryWrapper<Document> docLqw = new LambdaQueryWrapper<>();
        docLqw.in(Document::getKnowledgeId,kbId);
        mindDocumentService.remove(docLqw);

        //删除知识库集合
        this.removeBatchByIds(kbId);

        //删除知识库缓存
        for (Long id : kbId) {
            iKnowledgeCacheService.deleteKnowledge(id);
        }
        //删除知识库数量缓存
        iKnowledgeCacheService.deleteKnowledgeCountNum();

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
