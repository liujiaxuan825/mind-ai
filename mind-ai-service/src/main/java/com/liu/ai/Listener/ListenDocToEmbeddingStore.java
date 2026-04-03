package com.liu.ai.Listener;

import com.liu.common.common.constant.MqConstant;
import com.liu.file.domain.Entity.Document;

import com.rabbitmq.client.Channel;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
@Component
public class ListenDocToEmbeddingStore {


    private final EmbeddingStoreIngestor ingestor;

    @RabbitListener(queues = MqConstant.QUEUE_DOCUMENT_MILVUS)
    public void getDoc(Document document, Channel channel, Message message) throws IOException {
        log.info("开始向量存储...");
        long tag = message.getMessageProperties().getDeliveryTag();
        if(StringUtils.isBlank(document.getContentText())){
            log.info("文档内容为空，不进行嵌入存储");
            channel.basicAck(tag, false);
        }
        dev.langchain4j.data.document.Document embeddingDocument = dev.langchain4j.data.document.Document.document(document.getContentText());
        Metadata metadata = embeddingDocument.metadata();
        metadata.put("documentId", document.getId());
        metadata.put("title", document.getName());
        metadata.put("userId", String.valueOf(document.getCreatedByUserId()));
        metadata.put("knowledgeId", document.getKnowledgeId());
        metadata.put("createdTime", document.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        try {
            ingestor.ingest(embeddingDocument);
            channel.basicAck(tag, false);
        } catch (IOException e) {
            log.error("文档嵌入存储失败", e);
            channel.basicNack(tag, false, false);
        }
    }
}
