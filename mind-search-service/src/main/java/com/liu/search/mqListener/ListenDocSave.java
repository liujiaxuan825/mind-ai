package com.liu.search.mqListener;

import com.liu.common.common.domain.DocumentMqMsgDTO;
import com.liu.search.config.EsDocumentRepository;
import com.liu.search.domain.Entity.EsDocument;
import com.liu.common.common.constant.MqConstant;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class ListenDocSave {

    private final EsDocumentRepository esDocumentRepository;


    private final ElasticsearchOperations elasticsearchOperations;


    private static final int CHUNK_SIZE = 300;
    private static final int CHUNK_OVERLAP = 30;
    private static final int BATCH_SIZE = 500;

    @RabbitListener(queues = MqConstant.QUEUE_DOCUMENT_SAVE)
    public void saveDocToEs(DocumentMqMsgDTO document, Channel channel, Message message) throws IOException {
        log.info("收到解析完成的文档，开始进行es切片存储，文档id : {}", document.getId());
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            String content = document.getContentText();
            if (content == null || content.isEmpty()) {
                channel.basicAck(tag, false);
                log.info("文本为空，不进行es存储");
                return;
            }
            // 调用批量处理方法
            processAndSaveChunksBatch(document, content);
            channel.basicAck(tag, false);
            log.info("文档ES切片存储完成，id : {}", document.getId());

        } catch (Exception e) {
            channel.basicNack(tag, false, false);
            log.error("文档es存储失败", e);
            e.printStackTrace();
        }
    }

    /**
     * 【优化后】流式分块 + 批量保存 -> 超快、不OOM、不MQ超时
     */
    private void processAndSaveChunksBatch(DocumentMqMsgDTO document, String content) {
        int length = content.length();
        int start = 0;
        int index = 0;

        List<EsDocument> batchList = new ArrayList<>(BATCH_SIZE);

        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);
            if (end < length) {
                int bestSplit = end;
                int searchFrom = Math.max(start + CHUNK_SIZE / 2, end - 100);

                for (int j = end - 1; j >= searchFrom; j--) {
                    char c = content.charAt(j);
                    if (c == '。' || c == '！' || c == '？' || c == '\n' || c == '.' || c == '!' || c == '?') {
                        bestSplit = j + 1;
                        break;
                    }
                }
                end = bestSplit;
            }

            String chunk = content.substring(start, end).trim();
            if (!chunk.isBlank()) {
                EsDocument esDoc = TextToEsDoc(document, chunk, index++);
                batchList.add(esDoc);
                if (batchList.size() >= BATCH_SIZE) {
                    elasticsearchOperations.save(batchList);
                    batchList.clear();
                }
            }

            start = end;

            if (start < length) {
                start = Math.max(0, start - CHUNK_OVERLAP);
            }
        }
        if (!batchList.isEmpty()) {
            elasticsearchOperations.save(batchList);
        }
    }

    private EsDocument TextToEsDoc(DocumentMqMsgDTO document, String chunk, int index) {
        EsDocument esDocument = new EsDocument();
        esDocument.setId(document.getId() + "_" + index);
        esDocument.setTitle(document.getName());
        esDocument.setContent(chunk);
        esDocument.setOriginalDocId(document.getId().toString());
        esDocument.setAuthor(document.getCreatedByUserId().toString());
        esDocument.setKnowledgeId(document.getKnowledgeId());
        esDocument.setCreateTime(document.getCreatedTime().toLocalDate());
        esDocument.setUpdateTime(document.getUpdatedTime().toLocalDate());
        return esDocument;
    }
}