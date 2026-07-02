package com.liu.ai.Listener;

import com.liu.ai.common.DocumentMqMsgDTO;
import com.liu.ai.common.MqConstant;
import com.rabbitmq.client.Channel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
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
    public void listenMilvusDelete(DocumentMqMsgDTO document, Channel channel, Message message) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        log.info("向量数据库收到删除文档消息，文档id: {}, userId: {}", document.getId(), document.getCreatedByUserId());
        try {
            Filter documentIdFilter = new IsEqualTo("documentId", String.valueOf(document.getId()));
            Filter userIdFilter = new IsEqualTo("userId", String.valueOf(document.getCreatedByUserId()));
            Filter filter = Filter.and(documentIdFilter, userIdFilter);
            embeddingStore.removeAll(filter);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("删除向量数据库文档失败，文档id: {}", document.getId(), e);
            channel.basicNack(tag, false, false);
        }
    }
}