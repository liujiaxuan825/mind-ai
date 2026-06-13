package com.liu.file.controller;

import com.liu.file.Service.IMindDocumentService;
import com.liu.file.domain.VO.DocumentVO;
import com.liu.common.common.Result;
import com.liu.common.common.page.PageRequestDTO;
import com.liu.common.common.page.PageResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author liujiaxuan
 * @since 2025-11-19
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/document")
@RefreshScope
public class MindDocumentController {

    private final IMindDocumentService mindDocumentService;

    /**
     * 添加文档
     * @param klId
     * @param file
     * @return
     */
    @PostMapping("/add/{klId}")
    public Result<String> addDocument(@PathVariable Long klId,
                                    @RequestPart("file") MultipartFile file) {
        return mindDocumentService.addDocument(klId,file);
    }

    /**
     * 查询指定知识库中的文档
     * @param page
     * @param kbId
     * @return
     */
    @PostMapping("/list/{kbId}")
    public Result<PageResultVO<DocumentVO>> pageSelectDoc(@RequestBody PageRequestDTO page, @PathVariable Long kbId){
        return mindDocumentService.pageSelect(page,kbId);
    }

    /**
     * 详细查看文档界面
     * @param docId
     * @return
     */
    @GetMapping("/{docId}")
    public Result<DocumentVO> getDocument(@PathVariable Long docId) {
        return mindDocumentService.getDocument(docId);
    }

    /**
     * 删除文档
     * @param docId
     * @return
     */
    @DeleteMapping("/{docId}")
    public Result<Void> deleteDocument(@PathVariable Long docId) {
        mindDocumentService.deleteDocument(docId);
        return Result.success();
    }

    @GetMapping("/count")
    public Result<Long> countDocumentNum(){
        return mindDocumentService.countDocumentNum();
    }

    /**
     * 重新解析文档
     * @param docId
     * @return
     */
    @PostMapping("/reparse/{docId}")
    public Result<String> reParseDocument(@PathVariable Long docId){
        return mindDocumentService.reDocParse(docId);
    }
}