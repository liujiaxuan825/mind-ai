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
    private final StringRedisTemplateConfig.RedisCacheUtils redisCacheUtils;
    private final static int MAX_PARSE_COUNT = 3;

    @RabbitListener(queues = MqConstant.QUEUE_DOCUMENT_PARSE)
    public void listenDocParse(Document documentRecord, Channel channel, Message message) throws IOException {
        log.info("收到文档解析消息: {}", documentRecord);
        long tag = message.getMessageProperties().getDeliveryTag();
        if (documentRecord == null) {
            channel.basicAck(tag, false);
            return;
        }
        String key = "mq_doc_parse_" + documentRecord.getId().toString();
        try {
            iDocumentService.DocParse(documentRecord);
            channel.basicAck(tag, false);
            redisCacheUtils.delete(key);
        }catch (Exception e){
            log.error("文档解析失败，文档ID: {}", documentRecord.getId(), e);
            int count = 0;
            String parseCount = redisCacheUtils.get(key, String.class);
            if(StringUtils.isNotBlank(parseCount)){
                count = Integer.parseInt(parseCount);
            }
            if(count >= MAX_PARSE_COUNT){
                log.error("文档解析次数超过最大次数，文档ID: {}", documentRecord.getId());
                channel.basicNack(tag, false, false);
                redisCacheUtils.delete(key);
                mindDocumentServiceImpl.updateDocumentStatus(documentRecord, DocumentStatus.FAILED, "文档解析次数超过最大次数, 进入死信队列");
            }else {
                int newCount = count + 1;
                log.info("文档解析次数增加，文档ID: {}, 新次数: {}", documentRecord.getId(), newCount);
                redisCacheUtils.setWithRandomExpire(key, String.valueOf(newCount), 60);
                channel.basicNack(tag, false, true);
            }
        }
    }
}