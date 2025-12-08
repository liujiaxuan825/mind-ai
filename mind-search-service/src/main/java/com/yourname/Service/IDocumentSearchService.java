package com.yourname.Service;

import com.yourname.domain.DTO.GlobalSearchDTO;
import com.yourname.domain.VO.GlobalSearchResultVO;
import com.yourname.mind.common.Result;
import com.yourname.mind.common.page.PageRequestDTO;
import com.yourname.mind.common.page.PageResultVO;

public interface IDocumentSearchService {
    Result<PageResultVO<GlobalSearchResultVO>> search(GlobalSearchDTO dto, PageRequestDTO page);
}
