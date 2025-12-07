package com.yourname.domain.VO;

import lombok.Data;

import java.util.List;

@Data
public class SearchResult {
    // 核心数据
    private List<EsDocumentVO> documents;
    // 分页信息
    private Long total; // 匹配到的总记录数，从SearchHits.getTotalHits()获取

    private Integer currentPage;

    private Integer pageSize;
    // 可便捷计算
    public Integer getTotalPages() {
        if (pageSize == null || pageSize == 0) return 0;
        return (int) Math.ceil((double) total / pageSize);
    }
}