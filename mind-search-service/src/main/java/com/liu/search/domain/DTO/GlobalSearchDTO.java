package com.liu.search.domain.DTO;

import com.liu.common.common.page.PageRequestDTO;
import lombok.Data;

@Data
public class GlobalSearchDTO {

    /**
     * 关键词匹配
     */
    private String keyWord;

    /**
     * 分页参数
     */
    private PageRequestDTO page;

}
