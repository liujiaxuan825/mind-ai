package com.yourname.domain.VO;


import lombok.Data;

import java.util.List;

@Data
public class EsDocumentSearchVO {

    /**
     * es文档基础类
     */
    private EsDocument esDocument;

    /**
     * 匹配的相关性得分
     */
    private Float score;

    /**
     * 高亮片段列表
     */
    private List<String> highlightSnippets;

    /**
     * 用于实现滑动分页的游标标识
     */
    private String cursor;
}
