package com.yourname.Service;

import com.yourname.domain.VO.SearchResult;
import com.yourname.mind.common.page.PageRequestDTO;

public interface IDocumentSearchService {
    SearchResult search(SearchRequestDTO dto, PageRequestDTO page);
}
