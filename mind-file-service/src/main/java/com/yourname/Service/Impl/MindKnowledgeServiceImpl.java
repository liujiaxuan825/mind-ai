package com.yourname.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.Service.IMindDocumentService;
import com.yourname.domain.DTO.KnowledgeDTO;
import com.yourname.domain.Entity.Document;
import com.yourname.domain.Entity.Knowledge;
import com.yourname.mapper.MindKnowledgeMapper;
import com.yourname.Service.IMindKnowledgeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yourname.domain.VO.KnowledgeVO;
import com.yourname.mind.common.Result;
import com.yourname.mind.common.page.PageRequestDTO;
import com.yourname.mind.common.page.PageResultVO;
import com.yourname.mind.config.UserContextHolder;
import com.yourname.mind.exception.BusinessException;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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


    @Override
    public void addKnowledge(KnowledgeDTO knowledgeDTO) {
        Long userId = UserContextHolder.getCurrentUserId();
        Knowledge knowledge = new Knowledge();
        BeanUtils.copyProperties(knowledgeDTO, knowledge);
        knowledge.setUserId(userId);
        this.save(knowledge);
    }

    @Override
    public Result<PageResultVO<KnowledgeVO>> pageSelect(PageRequestDTO pageDTO) {


        Page<Knowledge> page = this.page(pageDTO.toMpPage());
        List<KnowledgeVO> voList = page.getRecords().stream().map(item -> BeanUtil.copyProperties(item, KnowledgeVO.class)).collect(Collectors.toList());
        PageResultVO<KnowledgeVO> result = PageResultVO.success(voList, page.getTotal(), pageDTO);
        return Result.success(result);
    }

    private LambdaQueryWrapper<Knowledge> builderWrapper(KnowledgeDTO dto) {
        LambdaQueryWrapper<Knowledge> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Knowledge::getUserId, UserContextHolder.getCurrentUserId());
        lqw.like(StringUtils.isNotBlank(dto.getName()),Knowledge::getName,dto.getName());
        lqw.eq(StringUtils.isNotBlank(dto.getFilter()),Knowledge::getFilter,dto.getFilter());
        return lqw;
    }


    @Override
    @Transactional
    public void deleteKnowledge(List<Long> kbId) {
        LambdaQueryWrapper<Knowledge> knowLqw = new LambdaQueryWrapper<>();
        knowLqw.in(Knowledge::getUserId, kbId);
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
    }

    @Override
    public void updateKnowledge(KnowledgeDTO knowledgeDTO) {
        Long userId = UserContextHolder.getCurrentUserId();

    }

}
