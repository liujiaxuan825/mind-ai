package com.liu.search.domain.VO;

import com.liu.common.common.domain.ComKnowledgeVO;
import lombok.Data;
@Data
public class GlobalSearchResultVO {

    /**
     * 返回知识库的基本信息
     */
    private ComKnowledgeVO knowledgeVO;

    /**
     * 知识库中相关文档的数量
     */
    private Long relatedCount;
}