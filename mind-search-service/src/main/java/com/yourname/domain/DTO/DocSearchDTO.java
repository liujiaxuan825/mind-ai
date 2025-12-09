package com.yourname.domain.DTO;

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


}
