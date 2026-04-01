package com.liu.search.feign;

import com.liu.file.domain.VO.KnowledgeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "mind-file-service")
public interface KnowledgeFeignClient {

    @PostMapping("api/knowledge/list")
    List<KnowledgeVO> list(@RequestBody List<Long> kbId);
}
