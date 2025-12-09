package com.yourname.controller;

import com.yourname.Service.IDocumentSearchService;
import com.yourname.domain.DTO.DocSearchDTO;
import com.yourname.domain.DTO.GlobalSearchDTO;
import com.yourname.domain.VO.EsDocumentSearchVO;
import com.yourname.domain.VO.GlobalSearchResultVO;
import com.yourname.mind.common.Result;
import com.yourname.mind.common.page.PageRequestDTO;
import com.yourname.mind.common.page.PageResultVO;
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

    /**
     * 全局搜索,知识库界面搜索全部文档
     * @param dto
     * @param page
     * @return
     * @throws IOException
     */
    @GetMapping("/global/page")
    public Result<PageResultVO<GlobalSearchResultVO>> pageSearch(@RequestBody GlobalSearchDTO dto, @RequestBody PageRequestDTO page) throws IOException {
        return documentSearchService.search(dto,page);
    }


    /**
     * 单个知识库对文档搜索
     * @param dto
     * @param page
     * @return
     */
    @GetMapping("/document/page")
    public Result<PageResultVO<EsDocumentSearchVO>> docSearch(@RequestBody DocSearchDTO dto, @RequestBody PageRequestDTO page){
        return documentSearchService.docSearch(dto,page);
    }

}
