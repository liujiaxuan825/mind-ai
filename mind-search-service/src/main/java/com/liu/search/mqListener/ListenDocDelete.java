package com.liu.search.mqListener;

import com.liu.common.common.constant.MqConstant;
import com.liu.file.domain.Entity.Document;
import com.liu.search.config.EsDocumentRepository;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ListenDocDelete {

    private final EsDocumentRepository esDocumentRepository;

    @RabbitListener(queues = MqConstant.QUEUE_DOCUMENT_ES_DELETE)
    public void listenDocumentEs(Document documentRecord, Channel channel, Message message) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        log.info("收到删除文档消息: {}", documentRecord);
        try {
            // 删除es文档
            esDocumentRepository.deleteByOriginalDocId(documentRecord.getId().toString());
            // 手动确认消息
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("删除es文档失败，文档id：{}", documentRecord.getId(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
