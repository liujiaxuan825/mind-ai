package com.yourname.Service;

import com.yourname.domain.Entity.Document;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yourname.domain.VO.DocumentVO;
import com.yourname.mind.common.Result;
import com.yourname.mind.common.page.PageRequestDTO;
import com.yourname.mind.common.page.PageResultVO;
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
}
