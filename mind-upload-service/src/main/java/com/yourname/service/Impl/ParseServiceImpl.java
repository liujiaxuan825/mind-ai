package com.yourname.service.Impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yourname.Service.IDocumentSearchService;
import com.yourname.domain.Entity.Document;
import com.yourname.domain.enumsPack.DocumentStatus;
import com.yourname.mapper.MindDocumentMapper;
import com.yourname.mind.aliyun.AliyunOssUtil;
import com.yourname.mind.exception.BusinessException;
import com.yourname.service.IParseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParseServiceImpl implements IParseService {

    private final AliyunOssUtil aliyunOssUtil;

    private final Tika tika;

    private final MindDocumentMapper mindDocumentMapper;

    private final IDocumentSearchService iDocumentSearchService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void DocParse(Document documentRecord) {
        //更新文档状态为解析中
        updateDocumentStatus(documentRecord,DocumentStatus.PARSING,null);

        String fileKey = documentRecord.getFileKey();
        Path tempFile = null;
        try {
            // 1. 下载OSS文件到临时目录（核心逻辑保留）
            tempFile = aliyunOssUtil.downloadToTemp(fileKey);
            log.info("文件下载完成，路径: {}", tempFile);

            // 2. 解析文本（核心逻辑保留，简化清洗规则）
            String content = parseFileContent(tempFile);
            log.info("文档解析完成，字符数: {}", content.length());

            // 3. 提取页数（极简版：PDF精准，其他估算）
            Integer pageCount = getPageCount(tempFile, documentRecord.getMimeType());
            log.info("文档页数: {}", pageCount);

            // 4. 更新数据库（核心逻辑保留）
            updateDocumentContent(documentRecord, content, pageCount);

            // 5. 把数据一同存入es当中,为全文检索做准备
            iDocumentSearchService.saveDocToEs(documentRecord,content,pageCount);

        } catch (Exception e) {
            log.error("文档解析/es写入失败，文档ID: {}", documentRecord.getId(), e);
            updateDocumentStatus(documentRecord, DocumentStatus.FAILED, e.getMessage());
            throw new BusinessException("文档解析/es写入失败!");
        } finally {
            // 5. 简化的临时文件清理（单次重试，砍掉锁定检查）
            cleanupTempFileSimple(tempFile);
        }
    }

    private void updateDocumentStatus(Document documentRecord, DocumentStatus status, String msg){
        LambdaUpdateWrapper<Document> luw = new LambdaUpdateWrapper<>();
        luw.set(Document::getStatus,status);
        if(!msg.isEmpty()){
            luw.set(Document::getParseErrorMessage,msg);
        }
        mindDocumentMapper.update(documentRecord,luw);
    }

    // ========== 核心简化：文本解析（只保留必要清洗） ==========
    private String parseFileContent(Path tempFile) {
        try {
            String content = tika.parseToString(tempFile);
            return content == null ? "" : content
                    .replaceAll("\\s+", " ")        // 所有空白符（换行/空格）→ 单个空格
                    .replaceAll("[\\x00-\\x1F\\x7F]", "") // 移除控制字符
                    .trim();
        } catch (IOException | TikaException e) {
            log.error("文本解析失败", e);
            return "";
        }
    }

    // ========== 核心简化：页数提取（仅PDF精准，其他估算） ==========
    private Integer getPageCount(Path tempFile, String mimeType) {
        // PDF精准提取
        if ("application/pdf".equals(mimeType)) {
            try (PDDocument document = PDDocument.load(tempFile.toFile())) {
                return document.getNumberOfPages();
            } catch (Exception e) {
                log.warn("PDF页数提取失败，按内容估算", e);
            }
        }

        // 所有非PDF/提取失败的情况：按字符数估算（统一逻辑）
        try {
            String content = tika.parseToString(tempFile);
            return estimatePageCount(content);
        } catch (Exception e) {
            log.warn("页数估算失败，默认1页", e);
            return 1;
        }
    }

    // 统一的页数估算逻辑（砍掉所有分支，极简）
    private Integer estimatePageCount(String content) {
        if (content == null || content.isEmpty()) return 1;
        int charCount = content.length();
        // 经验值：每页约2000字符（可根据你的文档场景调整）
        return Math.max(1, (int) Math.ceil((double) charCount / 2000));
    }


    private void updateDocumentContent(Document documentRecord, String content, Integer pageCount) {
        LambdaUpdateWrapper<Document> luw = new LambdaUpdateWrapper<>();
        luw.set(Document::getContentText, content)
                .set(Document::getPageCount, pageCount);
        mindDocumentMapper.update(documentRecord,luw);

    }

    private void cleanupTempFileSimple(Path tempFile) {
        //阿里云工具中清理过一次了,进行一次判断
        if (tempFile == null || !Files.exists(tempFile)) return;

        try {
            Files.deleteIfExists(tempFile);
            log.debug("临时文件删除成功: {}", tempFile);
        } catch (IOException e) {
            // 单次重试
            try {
                Thread.sleep(500);
                Files.deleteIfExists(tempFile);
                log.debug("重试删除临时文件成功: {}", tempFile);
            } catch (Exception ex) {
                log.warn("临时文件删除失败，加入定时清理: {}", tempFile, ex);
                addToManualCleanupList(tempFile);
            }
        }
    }

    // ========== 兜底：定时清理（保留原有逻辑，无修改） ==========
    private void addToManualCleanupList(Path filePath) {
        try {
            Path cleanupLog = Paths.get(System.getProperty("java.io.tmpdir"), "smart_kb_cleanup.log");
            Files.write(cleanupLog,
                    (filePath + " - " + LocalDateTime.now() + "\n").getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("记录清理日志失败", e);
        }
    }

    // 定时任务调用：批量清理过期临时文件
    public void cleanupAllTempFiles() {
        try {
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            Files.list(tempDir)
                    .filter(path -> path.getFileName().toString().startsWith("oss_temp_") ||
                            path.getFileName().toString().startsWith("doc_parse_"))
                    .filter(path -> {
                        try {
                            Instant fileTime = Files.getLastModifiedTime(path).toInstant();
                            return fileTime.isBefore(Instant.now().minus(Duration.ofHours(1)));
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(this::cleanupTempFileSimple);
        } catch (IOException e) {
            log.error("批量清理临时文件失败", e);
        }
    }
}