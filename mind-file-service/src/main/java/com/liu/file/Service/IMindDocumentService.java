package com.liu.file.Service;

import com.liu.file.domain.Entity.Document;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.file.domain.VO.DocumentVO;
import com.liu.common.common.Result;
import com.liu.common.common.page.PageRequestDTO;
import com.liu.common.common.page.PageResultVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liujiaxuan
 * @since 2025-11-19
 */
public interface IMindDocumentService extends IService<Document> {

    Result<String> addDocument(Long klId, MultipartFile file);

    Result<PageResultVO<DocumentVO>> pageSelect(PageRequestDTO page, Long kbId);

    Result<DocumentVO> getDocument(Long docId);

    void deleteDocument(Long docId);

    Result<Long> countDocumentNum();

    void DocParse(Document documentRecord);

    Result<String> reDocParse(Long docId);
}
