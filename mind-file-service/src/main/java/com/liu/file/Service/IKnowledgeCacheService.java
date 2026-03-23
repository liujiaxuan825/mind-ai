package com.liu.file.Service;

import com.liu.file.domain.Entity.Knowledge;
import com.liu.file.domain.VO.KnowledgeVO;

import java.util.List;

public interface IKnowledgeCacheService {

    KnowledgeVO getKnowledgeById(Long id);

    void updateKnowledge(Knowledge knowledge);

    void deleteKnowledge(Long id);

    List<KnowledgeVO> getKnowledgeList(List<Long> ids);

    Long knowledgeCountNum();

    void deleteKnowledgeCountNum();
}
