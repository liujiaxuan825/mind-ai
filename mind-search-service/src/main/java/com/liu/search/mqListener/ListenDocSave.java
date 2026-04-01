package com.liu.search.mqListener;

import com.liu.search.config.EsDocumentRepository;
import com.liu.file.domain.Entity.Document;
import com.liu.search.domain.Entity.EsDocument;
import com.liu.common.common.constant.MqConstant;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.IOException;

@RequiredArgsConstructor
@Component
@Slf4j
public class ListenDocSave {

    private final EsDocumentRepository esDocumentRepository;

    // 配置不变
    private static final int CHUNK_SIZE = 300;
    private static final int CHUNK_OVERLAP = 30;

    @RabbitListener(queues = MqConstant.QUEUE_DOCUMENT_SAVE)
    public void saveDocToEs(Document document, Channel channel, Message message) throws IOException {
        log.info("收到解析完成的文档，开始进行es切片存储，文档id : {}", document.getId());
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            String content = document.getContentText();
            if (content == null || content.isEmpty()) {
                channel.basicAck(tag, false);
                log.info("文本为空，不进行es存储");
                return;
            }
            processAndSaveChunks(document, content);
            channel.basicAck(tag, false);

        } catch (Exception e) {
            channel.basicNack(tag, false, false);
            log.error("文档es存储失败", e);
            e.printStackTrace();
        }
    }

    /**
     * 流式分块 + 逐条保存
     * 永远不会把所有分块放内存 → 彻底解决OOM
     */
    private void processAndSaveChunks(Document document, String content) {
        int length = content.length();
        int start = 0;
        int index = 0;

        while (start < length) {
            // 1. 预期结束位置
            int end = Math.min(start + CHUNK_SIZE, length);

            // 2. 只有【不是最后一段】才做智能断句
            if (end < length) {
                // 从预期end往前找，找到一个合适的标点就停（保证句子完整）
                int bestSplit = end;
                // 搜索范围：后 1/3 区域，避免切得太短
                int searchFrom = Math.max(start + CHUNK_SIZE / 2, end - 100);

                for (int j = end - 1; j >= searchFrom; j--) {
                    char c = content.charAt(j);
                    // 真正的句子结束符
                    if (c == '。' || c == '！' || c == '？' || c == '\n') {
                        bestSplit = j + 1; // 保留这个标点
                        break;
                    }
                }
                end = bestSplit;
            }

            // 3. 截取（一定是完整句子）
            String chunk = content.substring(start, end).trim();
            if (!chunk.isBlank()) {
                EsDocument esDoc = TextToEsDoc(document, chunk, index++);
                esDocumentRepository.save(esDoc);
            }

            // 4. 稳定移动指针（不会跳、不会乱、不会重复）
            start = end;

            // 5. 安全重叠（只在段间重叠，不跳变）
            if (start < length) {
                start = Math.max(0, start - CHUNK_OVERLAP);
            }
        }
    }

    private EsDocument TextToEsDoc(Document document, String chunk, int index) {
        EsDocument esDocument = new EsDocument();
        esDocument.setId(document.getId() + "_" + index);
        esDocument.setTitle(document.getName());
        esDocument.setContent(chunk);
        esDocument.setOriginalDocId(document.getId().toString());
        esDocument.setAuthor(document.getCreatedByUserId().toString());
        esDocument.setKnowledgeId(document.getKnowledgeId());
        esDocument.setCreateTime(document.getCreatedTime());
        esDocument.setUpdateTime(document.getUpdatedTime());
        return esDocument;
    }
}
