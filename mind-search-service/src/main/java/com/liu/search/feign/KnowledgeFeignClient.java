package com.liu.search.feign;

import com.liu.common.common.domain.ComKnowledgeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "mind-file-service")
public interface KnowledgeFeignClient {

    @PostMapping("api/knowledge/list")
    List<ComKnowledgeVO> list(@RequestBody List<Long> kbId);
}
