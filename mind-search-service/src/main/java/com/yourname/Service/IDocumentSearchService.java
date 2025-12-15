package com.yourname.Service;

import com.yourname.domain.DTO.DocSearchDTO;
import com.yourname.domain.DTO.GlobalSearchDTO;
import com.yourname.domain.DTO.SingleSearchDTO;
import com.yourname.domain.Entity.Document;
import com.yourname.domain.VO.EsDocumentSearchVO;
import com.yourname.domain.VO.GlobalSearchResultVO;
import com.yourname.mind.common.Result;
import com.yourname.mind.common.page.PageRequestDTO;
import com.yourname.mind.common.page.PageResultVO;

public interface IDocumentSearchService {
    Result<PageResultVO<GlobalSearchResultVO>> search(GlobalSearchDTO dto, PageRequestDTO page);

    Result<PageResultVO<EsDocumentSearchVO>> docSearch(DocSearchDTO dto, PageRequestDTO page);

    Result<EsDocumentSearchVO> singleSearch(SingleSearchDTO dto);

    void saveDocToEs(Document documentRecord, String content, Integer pageCount);
}
