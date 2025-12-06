package com.yourname.mqListener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yourname.Service.IMindDocumentService;
import com.yourname.domain.Entity.Document;
import com.yourname.domain.enumsPack.DocumentStatus;
import com.yourname.mind.aliyun.AliyunOssUtil;
import com.yourname.mind.common.constant.MqConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
public class ListenDocParse {
    private final Tika tika;
    private final IMindDocumentService mindDocumentService;
    private final AliyunOssUtil  aliyunOssUtil;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstant.QUEUE_DOCUNMENT_PARSE),
            exchange = @Exchange(name = MqConstant.EXCHANG_DOCUNMENT_PARSE,type = ExchangeTypes.TOPIC),
            key = {MqConstant.ROUNTKEY_DOCUNMENT_PARSE}
    )
    )
    public void listenDocParse(Document documentRecord) {
        if (documentRecord == null) {
            return;
        }
        String fileKey = documentRecord.getFileKey();
        updateDocumentStatus(documentRecord, DocumentStatus.PARSING, null);
        Path tempFile = null;
        try {
            // 1. 从阿里云OSS下载文件到临时目录
            tempFile = aliyunOssUtil.downloadToTemp(fileKey);
            log.info("文件下载完成，路径: {}", tempFile);

            // 2. 使用Tika解析文档内容
            String content = parseFileContent(tempFile);
            log.info("文档解析完成，字符数: {}", content.length());

            // 3. 提取文档页数
            Integer pageCount = extractPageCount(tempFile, documentRecord.getMimeType(),documentRecord.getFileExtension());
            log.info("文档页数: {}", pageCount);

            // 4. 更新文档内容到数据库
            updateDocumentContent(documentRecord, content, pageCount);
            log.info("文档内容保存完成，文档ID: {}", documentRecord.getId());

        } catch (Exception e) {
            log.error("文档解析失败，文档ID: {}", documentRecord.getId(), e);
            updateDocumentStatus(documentRecord, DocumentStatus.FAILED, e.getMessage());
        } finally {
            // 5. 清理临时文件
            if (tempFile != null) {
                cleanupTempFile(tempFile);
            }
        }
    }

    private Integer extractPageCount(Path tempFile, String mimeType,String fileExtension) throws IOException {
        try {
            if (mimeType == null && fileExtension != null) {
                mimeType = detectMimeTypeFromExtension(fileExtension);
            }

            if (mimeType != null) {
                switch (mimeType) {
                    case "application/pdf":
                        return extractPdfPageCount(tempFile);
                    case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                    case "application/msword":
                        return extractWordPageCount(tempFile, mimeType);
                    case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                    case "application/vnd.ms-powerpoint":
                        return extractPptPageCount(tempFile, mimeType);
                    case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                    case "application/vnd.ms-excel":
                        return extractExcelPageCount(tempFile);
                    default:
                        return estimatePageCountFromContent(tempFile);
                }
            } else {
                // 无法识别类型时，根据内容估算
                return estimatePageCountFromContent(tempFile);
            }
        } catch (Exception e) {
            log.warn("提取页数失败，文件: {}, 使用默认值1", tempFile.getFileName(), e);
            return 1; // 默认1页
        }
    }

    private String parseFileContent(Path tempFile) {
        try {
            String content = tika.parseToString(tempFile);
            if(content==null){
                return "";
            }
            return content.replaceAll("\\n{3,}", "\n\n")    // 多个换行 → 2个换行
                    .replaceAll("\\s{2,}", " ")        // 多个空格 → 1个空格
                    .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "") // 移除控制字符
                    .trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TikaException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateDocumentContent(Document documentRecord, String content, Integer pageCount) {
        LambdaUpdateWrapper<Document> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Document::getId, documentRecord.getId())
                     .set(Document::getContentText, content)
                     .set(Document::getPageCount, pageCount);
    }


    private void updateDocumentStatus(Document documentRecord, DocumentStatus documentStatus, Object o) {
        LambdaUpdateWrapper<Document> luw = new LambdaUpdateWrapper<>();
        luw.eq(Document::getId, documentRecord.getId()).set(Document::getStatus, documentStatus);
        mindDocumentService.update(luw);
    }
    private Integer extractPdfPageCount(Path pdfPath) {
        try {
            PDDocument document = PDDocument.load(pdfPath.toFile());
            int pageCount = document.getNumberOfPages();
            document.close();
            log.debug("PDF页数提取成功: {} 页", pageCount);
            return pageCount;
        } catch (Exception e) {
            log.warn("PDF页数提取失败，尝试备用方法", e);
            return estimatePageCountFromContent(pdfPath);
        }
    }

    /**
     * 提取Word文档页数（估算）
     */
    private Integer extractWordPageCount(Path wordPath, String mimeType) {
        try {
            // 使用Tika提取内容并估算
            String content = tika.parseToString(wordPath);
            return estimatePageCountByChars(content);
        } catch (Exception e) {
            log.warn("Word页数提取失败", e);
            return estimatePageCountFromContent(wordPath);
        }
    }

    /**
     * 提取PPT页数
     */
    private Integer extractPptPageCount(Path pptPath, String mimeType) {
        try {
            // PPT通常一页内容较少，按段落数估算
            String content = tika.parseToString(pptPath);
            int slideCount = content.split("\n\n").length; // 按空行分隔估算幻灯片数
            return Math.max(1, slideCount);
        } catch (Exception e) {
            log.warn("PPT页数提取失败", e);
            return 1;
        }
    }

    /**
     * 提取Excel页数（工作表数量）
     */
    private Integer extractExcelPageCount(Path excelPath) {
        try {
            // Excel通常一个工作表算一页
            String content = tika.parseToString(excelPath);
            // 简单估算：按工作表特征或内容量
            return Math.max(1, content.split("Sheet").length - 1);
        } catch (Exception e) {
            log.warn("Excel页数提取失败", e);
            return 1;
        }
    }

    /**
     * 根据字符数估算页数
     */
    private Integer estimatePageCountByChars(String content) {
        if (content == null || content.isEmpty()) {
            return 1;
        }

        int charCount = content.length();

        // 估算逻辑：
        if (charCount < 1000) return 1;                    // 1页以内
        else if (charCount < 3000) return 2;               // 1-2页
        else if (charCount < 6000) return 3;               // 2-3页
        else if (charCount < 10000) return 4;              // 3-4页
        else return Math.max(1, charCount / 2500);         // 每页约2500字符
    }

    /**
     * 通过内容分析估算页数
     */
    private Integer estimatePageCountFromContent(Path filePath) {
        try {
            String content = tika.parseToString(filePath);
            return estimatePageCountByChars(content);
        } catch (Exception e) {
            log.warn("内容页数估算失败", e);
            return 1;
        }
    }

    /**
     * 根据文件扩展名检测MIME类型
     */
    private String detectMimeTypeFromExtension(String fileExtension) {
        if (fileExtension == null) return null;

        switch (fileExtension.toLowerCase()) {
            case "pdf": return "application/pdf";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "doc": return "application/msword";
            case "pptx": return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "ppt": return "application/vnd.ms-powerpoint";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "xls": return "application/vnd.ms-excel";
            case "txt": return "text/plain";
            case "md": return "text/markdown";
            default: return null;
        }
    }

    private void cleanupTempFile(Path tempFile) {
        if (tempFile == null) {
            return;
        }

        int maxRetries = 3;
        int retryCount = 0;
        boolean deleted = false;

        while (retryCount < maxRetries && !deleted) {
            try {
                // 检查文件是否存在且可访问
                if (!Files.exists(tempFile)) {
                    log.debug("临时文件不存在，无需删除: {}", tempFile);
                    return;
                }

                // 检查文件是否被其他进程占用
                if (isFileLocked(tempFile)) {
                    log.warn("临时文件被占用，等待重试: {}", tempFile);
                    Thread.sleep(500); // 等待500ms
                    retryCount++;
                    continue;
                }

                // 删除文件
                deleted = Files.deleteIfExists(tempFile);

                if (deleted) {
                    log.debug("临时文件删除成功: {}", tempFile);
                } else {
                    log.warn("临时文件删除失败，文件不存在: {}", tempFile);
                }

            } catch (IOException e) {
                retryCount++;
                log.warn("临时文件删除失败，重试 {}/{}: {}", retryCount, maxRetries, tempFile, e);

                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(1000); // 等待1秒后重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("临时文件删除异常: {}", tempFile, e);
                break;
            }
        }

        if (!deleted) {
            log.error("临时文件最终删除失败，可能需要手动清理: {}", tempFile);
            // 可以在这里记录到需要手动清理的文件列表
            addToManualCleanupList(tempFile);
        }
    }

    /**
     * 检查文件是否被锁定（Windows系统常见问题）
     */
    private boolean isFileLocked(Path filePath) {
        try {
            // 尝试以写入模式打开文件，如果成功说明未被锁定
            Files.newByteChannel(filePath, StandardOpenOption.WRITE).close();
            return false;
        } catch (IOException e) {
            // 如果抛出异常，说明文件可能被锁定
            return true;
        }
    }

    /**
     * 添加到手动清理列表（用于后续批量清理）
     */
    private void addToManualCleanupList(Path filePath) {
        // 可以记录到日志文件、数据库或内存列表中
        log.warn("需要手动清理的文件: {}", filePath);

        // 简单的内存记录（生产环境建议用数据库或文件）
        try {
            Path cleanupLog = Paths.get(System.getProperty("java.io.tmpdir"), "smart_kb_cleanup.log");
            Files.write(cleanupLog,
                    (filePath.toString() + " - " + LocalDateTime.now() + System.lineSeparator()).getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("记录清理日志失败", e);
        }
    }

    /**
     * 批量清理临时文件（可用于定时任务）
     */
    public void cleanupAllTempFiles() {
        try {
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));

            Files.list(tempDir)
                    .filter(path -> path.getFileName().toString().startsWith("oss_temp_") ||
                            path.getFileName().toString().startsWith("doc_parse_"))
                    .filter(path -> {
                        try {
                            // 只清理创建时间超过1小时的文件
                            Instant fileTime = Files.getLastModifiedTime(path).toInstant();
                            Instant oneHourAgo = Instant.now().minus(Duration.ofHours(1));
                            return fileTime.isBefore(oneHourAgo);
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(this::cleanupTempFile);

        } catch (IOException e) {
            log.error("批量清理临时文件失败", e);
        }
    }
}
