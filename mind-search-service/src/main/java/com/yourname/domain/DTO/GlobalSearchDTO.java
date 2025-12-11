package com.yourname.domain.DTO;

import lombok.Data;

@Data
public class GlobalSearchDTO {

    /**
     * 关键词匹配
     */
    private String keyWord;

    /**
     * 知识库权限
     */
    private String filter;

}
