package com.liu.search.domain.DTO;

import com.liu.common.common.page.PageRequestDTO;
import lombok.Data;

@Data
public class DocSearchDTO {
    /**
     * 必须指定知识库
     */
    private Long knowledgeId;

    /**
     * 关键词
     */
    private String keyWord;

    /**
     * 分页参数
     */
    private PageRequestDTO page;


}
