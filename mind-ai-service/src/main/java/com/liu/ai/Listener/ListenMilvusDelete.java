package com.liu.ai.Listener;

import com.liu.common.common.constant.MqConstant;
import com.liu.file.domain.Entity.Document;
import com.rabbitmq.client.Channel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import static dev.langchain4j.store.embedding.filter.Filter.*;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ListenMilvusDelete {

    private final EmbeddingStore<TextSegment> embeddingStore;

    @RabbitListener(queues = MqConstant.QUEUE_DOCUMENT_MILVUS_DELETE)
    public void listenMilvusDelete(Document document, Channel channel, Message message) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        log.info("向量数据库收到删除文档消息，文档id: {}", document.getId());
        try {
            Filter filter = new IsEqualTo("documentId", document.getId());
            embeddingStore.removeAll(filter);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("删除向量数据库文档失败，文档id: {}", document.getId());
            channel.basicNack(tag, false, false);
        }
    }
}
