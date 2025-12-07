package com.yourname.controller;

import com.yourname.Service.IDocumentSearchService;
import com.yourname.domain.VO.SearchResult;
import com.yourname.mind.common.Result;
import com.yourname.mind.common.page.PageRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class DocumentSearchController {

    private final IDocumentSearchService documentSearchService;

    @GetMapping("/page")
    public Result<SearchResult> pageSearch(@RequestBody SearchRequestDTO dto, @RequestBody PageRequestDTO page) throws IOException {
        SearchResult result = documentSearchService.search(dto,page);
        return Result.success(result);
    }
}
