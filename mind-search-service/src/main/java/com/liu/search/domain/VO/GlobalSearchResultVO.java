package com.liu.search.domain.VO;

import com.liu.file.domain.VO.KnowledgeVO;
import lombok.Data;
@Data
public class GlobalSearchResultVO {

    /**
     * 返回知识库的基本信息
     */
    private KnowledgeVO knowledgeVO;

    /**
     * 知识库中相关文档的数量
     */
    private Long relatedCount;
}