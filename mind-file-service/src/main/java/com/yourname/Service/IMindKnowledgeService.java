package com.yourname.Service;

import com.yourname.domain.DTO.KnowledgeDTO;
import com.yourname.domain.Entity.Knowledge;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yourname.domain.VO.KnowledgeVO;
import com.yourname.mind.common.Result;
import com.yourname.mind.common.page.PageRequestDTO;
import com.yourname.mind.common.page.PageResultVO;

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
