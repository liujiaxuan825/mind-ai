package com.liu.file.Service;

import com.liu.file.domain.DTO.KnowledgeDTO;
import com.liu.file.domain.Entity.Knowledge;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.file.domain.VO.KnowledgeVO;
import com.liu.common.common.Result;
import com.liu.common.common.page.PageRequestDTO;
import com.liu.common.common.page.PageResultVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liujiaxuan
 * @since 2025-11-18
 */
public interface IMindKnowledgeService extends IService<Knowledge> {

    void addKnowledge(KnowledgeDTO knowledgeAddDTO);

    Result<PageResultVO<KnowledgeVO>> pageSelect(PageRequestDTO pageDTO);

    void deleteKnowledge(List<Long> kbId);

    void updateKnowledge(KnowledgeDTO knowledgeDTO);

    Result<Long> countKnowledgeNum();
}
