package com.yourname.Service;

import com.yourname.domain.Entity.Knowledge;
import com.yourname.domain.VO.KnowledgeVO;

import java.util.List;

public interface IKnowledgeCacheService {

    Knowledge getKnowledgeById(Long id);

    void updateKnowledge(Knowledge knowledge);

    void deleteKnowledge(Long id);

    List<KnowledgeVO> getKnowledgeList(List<Long> ids);
}
