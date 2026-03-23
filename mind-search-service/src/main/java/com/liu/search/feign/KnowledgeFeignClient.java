package com.liu.search.feign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liu.file.domain.Entity.Knowledge;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mind-file-service")
public interface KnowledgeFeignClient {

    @GetMapping("/list")
    List<Knowledge> list(@RequestParam LambdaQueryWrapper<Knowledge> queryWrapper);
}
