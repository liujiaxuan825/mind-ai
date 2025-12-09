package com.yourname.mind.common.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

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

    public Pageable convertToPageable() {
        // ========== 关键点1：页码转换 ==========
        // Spring Data 页码从0开始，你DTO中的pageNum从1开始
        int pageNumber = getPageNum() - 1;
        if (pageNumber < 0) pageNumber = 0;  // 防止负数

        int pageSize = Math.min(getPageSize(), 1000);  // 限制最大1000条

        // ========== 关键点3：排序处理 ==========
        if (getSortBy() != null && !getSortBy().trim().isEmpty()) {
            // 解析排序字段和方向
            Sort sort = parseSort(getSortBy());
            return PageRequest.of(pageNumber, pageSize, sort);
        }

        // 无排序的情况
        return PageRequest.of(pageNumber, pageSize);
    }
    private Sort parseSort(String sortBy) {
        String[] sortFields = sortBy.split(",");
        List<Sort.Order> orders = new ArrayList<>();

        for (String field : sortFields) {
            String[] parts = field.split(":");
            if (parts.length == 1) {
                // 默认升序
                orders.add(Sort.Order.asc(parts[0].trim()));
            } else if (parts.length == 2) {
                String fieldName = parts[0].trim();
                String direction = parts[1].trim().toLowerCase();

                if ("desc".equals(direction)) {
                    orders.add(Sort.Order.desc(fieldName));
                } else {
                    orders.add(Sort.Order.asc(fieldName));
                }
            }
        }

        return Sort.by(orders);
    }
}