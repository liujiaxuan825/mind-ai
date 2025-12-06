package com.yourname.mind.common.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class PageRequestDTO {
    
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String sortBy;

    public PageRequestDTO() {}
    
    public PageRequestDTO(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        this.pageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 1000);
    }
    
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
    
    public <T> Page<T> toMpPage() {
        return new Page<>(pageNum, pageSize);
    }
}