package com.yourname.domain.DTO;

import lombok.Data;

@Data
public class SingleSearchDTO {

    /**
     * 指定文档id
     */
    private Long documentId;

    /**
     * 关键词
     */
    private String keyWord;
}
