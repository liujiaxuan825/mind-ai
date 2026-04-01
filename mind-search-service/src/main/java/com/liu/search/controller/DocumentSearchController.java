package com.liu.search.controller;

import com.liu.search.Service.IDocumentSearchService;
import com.liu.search.domain.DTO.DocSearchDTO;
import com.liu.search.domain.DTO.GlobalSearchDTO;
import com.liu.search.domain.VO.EsDocumentSearchVO;
import com.liu.search.domain.VO.GlobalSearchResultVO;
import com.liu.common.common.Result;
import com.liu.common.common.page.PageRequestDTO;
import com.liu.common.common.page.PageResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class DocumentSearchController {

    private final IDocumentSearchService documentSearchService;

    /**
     * 全局搜索,知识库界面搜索全部文档
     * @param dto
     * @return
     * @throws IOException
     */
    @PostMapping("/global/data")
    public Result<PageResultVO<GlobalSearchResultVO>> pageSearch(@RequestBody GlobalSearchDTO dto) throws IOException {
        return documentSearchService.search(dto);
    }

    /**
     * 单个知识库对文档搜索
     * @param dto
     * @return
     */
    @PostMapping("/document/data")
    public Result<PageResultVO<EsDocumentSearchVO>> docSearch(@RequestBody DocSearchDTO dto){
        return documentSearchService.docSearch(dto);
    }

//    /**
//     * 单个文档的检索
//     * @param dto
//     * @return
//     */
//    @GetMapping("/single/{documentId}")
//    public Result<EsDocumentSearchVO> singleSearch(@RequestBody SingleSearchDTO dto){
//        return documentSearchService.singleSearch(dto);
//    }

}
