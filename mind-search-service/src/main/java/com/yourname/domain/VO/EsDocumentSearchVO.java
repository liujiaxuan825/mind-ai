package com.yourname.domain.VO;


import lombok.Data;

import java.util.List;

@Data
public class EsDocumentSearchVO {

    /**
     * es文档基础类
     */

    private EsDocumentVO esDocumentVO;

    /**
     * 匹配的相关性得分
     */
    private Float score;

    /**
     *
     */
    private List<String> highlightTitle;

    /**
     * 高亮片段列表
     */
    private List<String> highlightContent;


    /**
     * 用于实现滑动分页的游标标识
     */
    //private String cursor;
}
