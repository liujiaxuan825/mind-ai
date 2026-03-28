package com.liu.search.Service;

import com.liu.search.domain.DTO.DocSearchDTO;
import com.liu.search.domain.DTO.GlobalSearchDTO;
import com.liu.search.domain.DTO.SingleSearchDTO;
import com.liu.file.domain.Entity.Document;
import com.liu.search.domain.VO.EsDocumentSearchVO;
import com.liu.search.domain.VO.GlobalSearchResultVO;
import com.liu.common.common.Result;
import com.liu.common.common.page.PageRequestDTO;
import com.liu.common.common.page.PageResultVO;

public interface IDocumentSearchService {
    Result<PageResultVO<GlobalSearchResultVO>> search(GlobalSearchDTO dto, PageRequestDTO page);

    Result<PageResultVO<EsDocumentSearchVO>> docSearch(DocSearchDTO dto, PageRequestDTO page);

    Result<EsDocumentSearchVO> singleSearch(SingleSearchDTO dto);

    void saveDocToEs(Document documentRecord, String content, Integer pageCount);
}
