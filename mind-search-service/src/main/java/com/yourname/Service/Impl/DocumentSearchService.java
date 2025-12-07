package com.yourname.Service.Impl;

import com.yourname.Service.IDocumentSearchService;
import com.yourname.domain.VO.SearchResult;
import com.yourname.mind.common.page.PageRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class DocumentSearchService implements IDocumentSearchService {


    @Override
    public SearchResult search(SearchRequestDTO dto, PageRequestDTO page) {

    }
}
