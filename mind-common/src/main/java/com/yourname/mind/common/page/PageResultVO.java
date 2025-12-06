package com.yourname.mind.common.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jdk.jfr.Description;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class PageResultVO<T> {
    
    private Integer pageNum;
    private Integer pageSize;
    private Long total;
    private Integer totalPages;
    private List<T> list = new ArrayList<>();
    @Description("是否还有前一页")
    private Boolean hasPrevious;
    @Description("是否还有下一页")
    private Boolean hasNext;
    @Description("是否第一页")
    private Boolean isFirst;
    @Description("是否最后一页")
    private Boolean isLast;

    public PageResultVO() {}
    
    public PageResultVO(Integer pageNum, Integer pageSize, Long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list != null ? list : new ArrayList<>();
        
        this.totalPages = (pageSize == 0 || total == 0) ? 0 : (int) Math.ceil((double) total / pageSize);
        this.hasPrevious = pageNum > 1;
        this.hasNext = pageNum < totalPages;
        this.isFirst = pageNum == 1;
        this.isLast = pageNum.equals(totalPages) || totalPages == 0;
    }
    
    public static <T> PageResultVO<T> of(Page<T> page) {
        return new PageResultVO<>(
            (int) page.getCurrent(),
            (int) page.getSize(),
            page.getTotal(),
            page.getRecords()
        );
    }
    
    public static <T> PageResultVO<T> empty(PageRequestDTO pageRequest) {
        return new PageResultVO<>(
            pageRequest.getPageNum(),
            pageRequest.getPageSize(),
            0L,
            new ArrayList<>()
        );
    }
    
    public static <T> PageResultVO<T> success(List<T> list, Long total, PageRequestDTO pageRequest) {
        return new PageResultVO<>(
            pageRequest.getPageNum(),
            pageRequest.getPageSize(),
            total,
            list
        );
    }
}