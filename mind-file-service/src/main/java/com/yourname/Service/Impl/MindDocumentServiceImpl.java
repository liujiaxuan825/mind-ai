package com.yourname.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.domain.Entity.Document;
import com.yourname.mapper.MindDocumentMapper;
import com.yourname.Service.IMindDocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yourname.domain.VO.DocumentVO;
import com.yourname.domain.enumsPack.DocumentStatus;
import com.yourname.mind.aliyun.AliyunOssUtil;
import com.yourname.mind.common.Result;
import com.yourname.mind.common.constant.MqConstant;
import com.yourname.mind.common.page.PageRequestDTO;
import com.yourname.mind.common.page.PageResultVO;
import com.yourname.mind.config.UserContextHolder;
import com.yourname.mind.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liujiaxuan
 * @since 2025-11-19
 */
@Service
@RequiredArgsConstructor
public class MindDocumentServiceImpl extends ServiceImpl<MindDocumentMapper, Document> implements IMindDocumentService {

    /*private final IMindKnowledgeService mindKnowledgeService;*/

    private final AliyunOssUtil aliyunOssUtil;

    private final RabbitTemplate rabbitTemplate;
    

    @Override
    public Result<String> addDocument(Long klId, MultipartFile file) {
        /*if(!knowledgeBaseExists(klId)){
            throw new BusinessException("知识库不存在！请重新选择。");
        }*/
        //验证文件是否符合规则！
        validateFile(file);

        //阿里云上传文件
        String fileKey = aliyunOssUtil.uploadFile(file);

        // 6. 创建文档记录
        Document documentRecord = createDocumentRecord(klId, file, fileKey);

        // 7. 触发异步解析
        triggerDocumentParse(documentRecord);

        // 8. 返回VO对象
        return Result.success(fileKey);
    }


    @Override
    public Result<PageResultVO<DocumentVO>> pageSelect(PageRequestDTO page, Long kbId) {
        LambdaQueryWrapper<Document> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Document::getKnowledgeId, kbId)
                .eq(Document::getCreatedByUserId,UserContextHolder.getCurrentUserId());
        Page<Document> pageResult = page(page.toMpPage(), lqw);
        List<DocumentVO> docList = pageResult.getRecords().stream().map(item -> BeanUtil.copyProperties(item, DocumentVO.class)).toList();
        PageResultVO<DocumentVO> result = PageResultVO.success(docList, pageResult.getTotal(), page);
        return Result.success(result);
    }

    @Override
    public Result<DocumentVO> getDocument(Long docId) {
        LambdaQueryWrapper<Document> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Document::getId, docId)
        .eq(Document::getCreatedByUserId,UserContextHolder.getCurrentUserId());
        Document document = getOne(lqw);
        DocumentVO documentVO = BeanUtil.copyProperties(document, DocumentVO.class);
        return Result.success(documentVO);
    }

    @Override
    public void deleteDocument(Long docId) {
        LambdaQueryWrapper<Document> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Document::getId, docId)
                .eq(Document::getCreatedByUserId,UserContextHolder.getCurrentUserId());
        remove(lqw);
    }


    //异步处理解析文件
    private void triggerDocumentParse(Document documentRecord) {
        rabbitTemplate.convertAndSend(MqConstant.EXCHANG_DOCUNMENT_PARSE,MqConstant.ROUNTKEY_DOCUNMENT_PARSE,documentRecord);
    }

    //分析之后封装数据并添加至数据库
    private Document createDocumentRecord(Long klId, MultipartFile file, String fileKey) {
        Document document = new Document();
        document.setName(file.getOriginalFilename());
        document.setFileKey(fileKey);
        document.setFileSize(file.getSize());
        document.setKnowledgeId(klId);
        document.setMimeType(file.getContentType());
        document.setFileExtension(extractFileExtension(file.getOriginalFilename()));
        document.setStatus(DocumentStatus.UPLOADED);
        document.setCreatedByUserId(UserContextHolder.getCurrentUserId());
        this.save(document);
        return document;
    }

    private String extractFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        return (lastDotIndex > 0) ? filename.substring(lastDotIndex + 1) : "";
    }


    /*private boolean knowledgeBaseExists(Long knowledgeBaseId) {
        // 查询数据库验证知识库是否存在
        Knowledge kl = mindKnowledgeService.getById(knowledgeBaseId);
        return kl != null;
    }*/

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 验证文件大小 (100MB)
        long maxSize = 100 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小不能超过100MB");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (!isSupportedFileType(contentType)) {
            throw new BusinessException("不支持的文件格式");
        }
    }

    private boolean isSupportedFileType(String contentType) {
        return contentType != null && (
                contentType.startsWith("application/pdf") ||
                        contentType.startsWith("application/msword") ||
                        contentType.startsWith("application/vnd.openxmlformats-officedocument") ||
                        contentType.startsWith("text/") ||
                        contentType.equals("application/vnd.ms-excel") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                        contentType.equals("application/vnd.ms-powerpoint") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")
        );
    }
}
