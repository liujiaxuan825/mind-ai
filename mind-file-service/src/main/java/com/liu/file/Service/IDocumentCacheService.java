package com.liu.file.Service;

import com.liu.common.common.Result;
import com.liu.file.domain.VO.DocumentVO;

public interface IDocumentCacheService {

    Long countNum();

    void deleteCountNum();

    DocumentVO getDocument(Long docId);
}
