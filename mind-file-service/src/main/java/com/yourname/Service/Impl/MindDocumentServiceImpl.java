package com.yourname.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yourname.Service.IDocumentCacheService;
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
import com.yourname.service.IUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
@Slf4j
public class MindDocumentServiceImpl extends ServiceImpl<MindDocumentMapper, Document> implements IMindDocumentService {


    private final RabbitTemplate rabbitTemplate;



    private final IDocumentCacheService  documentCacheService;

    private final IUploadService uploadService;


    

    @Override
    public Result<String> addDocument(Long klId, MultipartFile file) {

        //直接调用统一上传文件服务
        String fileKey = uploadService.uploadFileToOss(file);

        // 创建文档记录
        Document documentRecord = createDocumentRecord(klId, file, fileKey);

        // 触发异步解析
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


    @Override
    public Result<Long> countDocumentNum() {
        Long num = documentCacheService.countNum();
        return Result.success(num);
    }


    //异步处理解析文件
    private void triggerDocumentParse(Document documentRecord) {
        rabbitTemplate.convertAndSend(MqConstant.EXCHANGE_DOCUMENT_PARSE,MqConstant.ROUT_KEY_DOCUMENT_PARSE,documentRecord);
    }

    //分析之后封装数据并添加至数据库
    private Document createDocumentRecord(Long klId, MultipartFile file, String fileKey) {
        Document document = new Document();
        document.setName(file.getOriginalFilename());
        document.setFileKey(fileKey);
        document.setFileSize(file.getSize());
        document.setKnowledgeId(klId);
        document.setMimeType(file.getContentType());
        document.setFileExtension(extractFileExtension(Objects.requireNonNull(file.getOriginalFilename())));
        document.setStatus(DocumentStatus.UPLOADED);
        document.setCreatedByUserId(UserContextHolder.getCurrentUserId());
        this.save(document);
        return document;
    }

    private String extractFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        return (lastDotIndex > 0) ? filename.substring(lastDotIndex + 1) : "";
    }

}
