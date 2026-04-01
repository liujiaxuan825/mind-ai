package com.liu.search.Service;

import com.liu.search.domain.DTO.DocSearchDTO;
import com.liu.search.domain.DTO.GlobalSearchDTO;
import com.liu.search.domain.VO.EsDocumentSearchVO;
import com.liu.search.domain.VO.GlobalSearchResultVO;
import com.liu.common.common.Result;
import com.liu.common.common.page.PageRequestDTO;
import com.liu.common.common.page.PageResultVO;

public interface IDocumentSearchService {

    Result<PageResultVO<GlobalSearchResultVO>> search(GlobalSearchDTO dto);

    Result<PageResultVO<EsDocumentSearchVO>> docSearch(DocSearchDTO dto);

//    Result<EsDocumentSearchVO> singleSearch(SingleSearchDTO dto);

}
