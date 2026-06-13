package com.liu.file.mqListener;

import com.liu.common.config.redisConfig.StringRedisTemplateConfig;
import com.liu.file.Service.IMindDocumentService;
import com.liu.file.Service.Impl.MindDocumentServiceImpl;
import com.liu.file.domain.Entity.Document;
import com.liu.common.common.constant.MqConstant;
import com.liu.file.domain.enumsPack.DocumentStatus;
import com.rabbitmq.client.Channel;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class ListenDocParse {

    private final IMindDocumentService iDocumentService;
    private final MindDocumentServiceImpl mindDocumentServiceImpl;

    @RabbitListener(queues = MqConstant.QUEUE_DOCUMENT_PARSE)
    public void listenDocParse(Document documentRecord) {
        log.info("收到文档解析消息: {}", documentRecord);
        if (documentRecord == null) {
            log.error("文档解析消息为空");
            throw new AmqpRejectAndDontRequeueException("无效消息");
        }
        //幂等性检查,mq消息重复消费时，避免重复解析文档
        Document nowDocument = mindDocumentServiceImpl.getById(documentRecord.getId());
        if (nowDocument.getStatus() == DocumentStatus.PARSING ||
                nowDocument.getStatus() == DocumentStatus.COMPLETED ||
                nowDocument.getStatus() == DocumentStatus.FAILED) {
            log.warn("文档状态为解析完成或解析失败，无需解析，文档ID: {}", nowDocument.getId());
            return;
        }
        try {
            iDocumentService.DocParse(documentRecord);
        }catch (Exception e){
            log.error("文档解析失败，文档ID: {}", documentRecord.getId(), e);
        }
    }
}