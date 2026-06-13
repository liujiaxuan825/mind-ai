package com.liu.file.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.liu.common.common.constant.RedisConstant;
import com.liu.common.common.domain.DocumentMqMsgDTO;
import com.liu.common.config.redisConfig.StringRedisTemplateConfig;
import com.liu.common.untils.UserContext;
import com.liu.file.Service.IDocumentCacheService;
import com.liu.file.config.RabbitMqSendUtil;
import com.liu.file.domain.Entity.Document;
import com.liu.file.feign.UploadFeignClient;
import com.liu.file.mapper.MindDocumentMapper;
import com.liu.file.Service.IMindDocumentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.file.domain.VO.DocumentVO;
import com.liu.file.domain.enumsPack.DocumentStatus;
import com.liu.file.Bloom.DocumentBloomFilterManager;
import com.liu.common.untils.AliyunOssUtil;
import com.liu.common.common.Result;
import com.liu.common.common.constant.MqConstant;
import com.liu.common.common.page.PageRequestDTO;
import com.liu.common.common.page.PageResultVO;
import com.liu.common.exception.BusinessException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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


    private final RabbitMqSendUtil rabbitMqSendUtil;

    private final IDocumentCacheService  documentCacheService;

    private final UploadFeignClient uploadFeignClient;

    private final AliyunOssUtil aliyunOssUtil;

    private final Tika tika;

    @Lazy
    private final DocumentBloomFilterManager documentBloomFilterManager;

    private final StringRedisTemplateConfig.RedisCacheUtils redisCacheUtils;

    private final Cache<String, DocumentVO> documentCache;

    private final MindDocumentServiceImpl selfService;


    @Override
    @SentinelResource(value = "file:addDocument", blockHandler = "addDocumentBlock")
    public Result<String> addDocument(Long klId, MultipartFile file) {
        if(klId == null){
            throw new BusinessException("知识库ID不能为空");
        }

        //直接调用统一上传文件服务
        Result<String> stringResult;
        try {
            stringResult = uploadFeignClient.uploadFile(file);
        } catch (FeignException e) {
            // Feign客户端异常
            log.error("调用上传服务失败, status:{}, message:{}", e.status(), e.getMessage(), e);
            throw new BusinessException("文件上传服务暂时不可用，请稍后重试");
        }

        if(stringResult == null || stringResult.getData() == null){
            throw new BusinessException("文档添加失败，文件上传失败");
        }
        String fileKey = stringResult.getData();

        Document documentRecord = null;
        try {
            //创建文档记录,保存到数据库
            documentRecord = createDocumentRecord(klId, file, fileKey);
        }catch (Exception e){
            //立即清除oss中的文件
            deleteDocInOss(fileKey);
            throw new BusinessException("数据库文档添加失败");
        }
        try {
            //保存到布隆过滤器
            documentBloomFilterManager.addDocumentToBloom(documentRecord.getId());
        }catch (Exception e){
            log.error("添加文档到布隆过滤器失败", e);
        }
        // 触发异步解析
        triggerDocumentParse(documentRecord);

        // 返回VO对象
        return Result.success(fileKey);
    }

    private void deleteDocInOss(String fileKey) {
        if(fileKey == null){
            return;
        }
        aliyunOssUtil.deleteFile(fileKey);
    }

    public Result<String> addDocumentBlock(Long klId, MultipartFile file, BlockException e) {
        log.error("文档添加失败，请求频率过高，请稍后重试", e);
        return Result.error("文档添加失败，请求频率过高，请稍后重试");
    }


    @Override
    public Result<PageResultVO<DocumentVO>> pageSelect(PageRequestDTO page, Long kbId) {
        if(page == null){
            throw new BusinessException("分页参数不能为空");
        }
        if(kbId == null){
            throw new BusinessException("知识库ID不能为空");
        }
        LambdaQueryWrapper<Document> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Document::getKnowledgeId, kbId)
                .eq(Document::getCreatedByUserId, UserContext.getUserId())
                .eq(Document::getIsDeleted, 0);
        Page<Document> pageResult = page(page.toMpPage(), lqw);
        List<DocumentVO> docList = pageResult.getRecords().stream().map(item -> BeanUtil.copyProperties(item, DocumentVO.class)).toList();
        PageResultVO<DocumentVO> result = PageResultVO.success(docList, pageResult.getTotal(), page);
        return Result.success(result);
    }


    @Override
    public Result<DocumentVO> getDocument(Long docId) {
        if(docId == null){
            throw new BusinessException("文档ID不能为空");
        }
        return Result.success(documentCacheService.getDocument(docId));
    }


    @Override
    public void deleteDocument(Long docId) {
        if(docId == null){
            throw new BusinessException("文档ID不能为空");
        }
        Long userId = UserContext.getUserId();
        if(userId == null){
            throw new BusinessException("登陆过期");
        }
        //1.删除前校验
        Document document = getById(docId);
        if (document == null || document.getIsDeleted() == 1) {
            throw new BusinessException("文档不存在或已被删除");
        }

        //2.软删除DB中文档记录
        boolean deleteSuccess = selfService.doDeleteTransaction(docId);
        if (!deleteSuccess) {
            throw new BusinessException("文档删除失败");
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                processAfterDeleteSuccess(document, userId);
            }
        });
    }

    private void processAfterDeleteSuccess(Document document, Long userId) {
        Long docId = document.getId();
        //添加禁用缓存标记
        String cacheKey = RedisConstant.DOCUMENT_CACHE_DISABLE + userId + "_" + docId;
        redisCacheUtils.setWithRandomExpire(cacheKey, "1", RedisConstant.DOCUMENT_CACHE_DISABLE_TTL);
        log.info("缓存禁用标记设置成功，docId: {}", docId);

        //删除文档redis缓存
        String key = RedisConstant.DOCUMENT_ID + userId + "_" + docId;
        documentCacheService.deleteCountNum();
        redisCacheUtils.delete(key);
        log.info("文档redis缓存第一次删除成功，docId: {}", docId);

        //删除本地缓存
        documentCache.invalidate(docId.toString());

        //8.发送延迟删除消息（终极兜底方案）TODO: 目前没有延迟
        rabbitMqSendUtil.sendMsg(MqConstant.EXCHANGE_DOCUMENT_REDIS_CACHE_DELETE,
                MqConstant.ROUT_KEY_DOCUMENT_REDIS_CACHE_DELETE, document, new CorrelationData(docId.toString()));

        sendDeleteIndexAndFileMsg(document);
    }

    private void sendDeleteIndexAndFileMsg(Document document) {
        //发送删除消息至es，向量数据库，oss存储
        DocumentMqMsgDTO documentMqMsgDTO = BeanUtil.copyProperties(document, DocumentMqMsgDTO.class);
        rabbitMqSendUtil.sendMsg(MqConstant.EXCHANGE_DOCUMENT_ES_MILVUS_OSS_DELETE,
                MqConstant.ROUT_KEY_DOCUMENT_ES_DELETE, documentMqMsgDTO , new CorrelationData(document.getId().toString()));

        rabbitMqSendUtil.sendMsg(MqConstant.EXCHANGE_DOCUMENT_ES_MILVUS_OSS_DELETE,
                MqConstant.ROUT_KEY_DOCUMENT_MILVUS_DELETE, documentMqMsgDTO , new CorrelationData(document.getId().toString()));

        rabbitMqSendUtil.sendMsg(MqConstant.EXCHANGE_DOCUMENT_ES_MILVUS_OSS_DELETE,
                MqConstant.ROUT_KEY_DOCUMENT_OSS_DELETE, document.getFileKey() , new CorrelationData(document.getId().toString()));
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean doDeleteTransaction(Long docId) {
        //1.软删除DB中文档记录
        LambdaUpdateWrapper<Document> lqw = new LambdaUpdateWrapper<>();
        lqw.eq(Document::getId, docId)
                .eq(Document::getIsDeleted, 0)
                .eq(Document::getCreatedByUserId, UserContext.getUserId())
                .set(Document::getUpdatedTime, LocalDateTime.now())
                .set(Document::getIsDeleted, 1);
        boolean result = this.update(lqw);
        log.info("文档软删除结果: {}, docId: {}", result, docId);
        return result;
    }


    @Override
    public Result<Long> countDocumentNum() {
        Long num = documentCacheService.countNum();
        return Result.success(num);
    }


    //异步处理解析文件
    private void triggerDocumentParse(Document documentRecord) {
        log.info("触发文档异步解析，文档ID: {}", documentRecord.getId());
        rabbitMqSendUtil.sendMsg(MqConstant.EXCHANGE_DOCUMENT_PARSE_ES_MILVUS, MqConstant.ROUT_KEY_DOCUMENT_PARSE,
                        documentRecord, new CorrelationData(documentRecord.getId().toString()));
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
        document.setCreatedByUserId(UserContext.getUserId());
        document.setCreatedTime(LocalDateTime.now());
        document.setUpdatedTime(LocalDateTime.now());
        document.setIsDeleted(0);
        this.save(document);
        return document;
    }


    private String extractFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        return (lastDotIndex > 0) ? filename.substring(lastDotIndex + 1) : "";
    }

    @Override
    public void DocParse(Document documentRecord) {
        //更新文档状态为解析中
        log.info("开始解析文档，文档ID: {}", documentRecord.getId());
        String fileKey = documentRecord.getFileKey();
        fileKey = fileKey.replaceFirst("^https?://.*?\\.aliyuncs\\.com/", "");
        Path tempFile = null;
        try {
            // 1. 下载OSS文件到临时目录
            tempFile = aliyunOssUtil.downloadToTemp(fileKey);
            log.info("文档下载完成，路径: {}", tempFile);

            // 2. 解析文本
            String content = parseFileContent(tempFile);
            log.info("文档解析完成，字符数: {}", content.length());

            // 3. 提取页数
            Integer pageCount = getPageCount(tempFile, documentRecord.getMimeType());
            log.info("文档页数: {}", pageCount);

            // 4. 更新数据库
            updateDocumentContent(documentRecord, content, pageCount);

            // 5.1 把数据一同存入es当中,为全文检索做准备
            documentRecord.setContentText(content);
            documentRecord.setPageCount(pageCount);

            // 5.2 转化为common中的DocumentMqMsgDTO
            DocumentMqMsgDTO documentMqMsgDTO = BeanUtil.copyProperties(documentRecord, DocumentMqMsgDTO.class);


            rabbitMqSendUtil.sendMsg(MqConstant.EXCHANGE_DOCUMENT_PARSE_ES_MILVUS, MqConstant.ROUT_KEY_DOCUMENT_SAVE,
                                documentMqMsgDTO, new CorrelationData(documentMqMsgDTO.getId().toString()));
            rabbitMqSendUtil.sendMsg(MqConstant.EXCHANGE_DOCUMENT_PARSE_ES_MILVUS, MqConstant.ROUT_KEY_DOCUMENT_MILVUS,
                                documentMqMsgDTO, new CorrelationData(documentMqMsgDTO.getId().toString()));

        } catch (Exception e) {
            log.error("文档解析/es写入失败，文档ID: {}", documentRecord.getId(), e);
            updateDocumentStatus(documentRecord, DocumentStatus.FAILED, "文档解析/es写入失败!");
            throw new BusinessException("文档解析/es写入失败!");
        } finally {
            // 5. 简化的临时文件清理（单次重试，砍掉锁定检查）
            cleanupTempFileSimple(tempFile);
        }
    }

    @Override
    public Result<String> reDocParse(Long docId) {
        Long userId = UserContext.getUserId();
        Document documentRecord = getOne(new LambdaQueryWrapper<Document>()
                .eq(Document::getCreatedByUserId, userId)
                .eq(Document::getId, docId));
        if (documentRecord == null) {
            return Result.error("文档不存在");
        }
        log.info("重新解析文档，文档ID: {}", documentRecord.getId());
        triggerDocumentParse(documentRecord);
        return Result.success("重新解析中...");
    }

    public void updateDocumentStatus(Document documentRecord, DocumentStatus status, String msg){
        LambdaUpdateWrapper<Document> luw = new LambdaUpdateWrapper<>();
        luw.eq(Document::getCreatedByUserId, documentRecord.getCreatedByUserId())
                .eq(Document::getId, documentRecord.getId())
                .set(Document::getStatus,status);
        if(!msg.isEmpty()){
            luw.set(Document::getParseErrorMessage,msg);
        }
        update(luw);
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
        luw.eq(Document::getCreatedByUserId, documentRecord.getCreatedByUserId())
                .eq(Document::getId, documentRecord.getId())
                .set(Document::getContentText, content)
                .set(Document::getPageCount, pageCount)
                .set(Document::getStatus, DocumentStatus.COMPLETED);
        update(luw);
    }

    private void cleanupTempFileSimple(Path tempFile) {
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
            Path cleanupLog = Paths.get(System.getProperty("java.io.tmpdir"), "mind_tempDoc_cleanup.log");
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
