package com.liu.ai.Listener;

import com.liu.ai.common.DocumentMqMsgDTO;
import com.liu.ai.common.MqConstant;
import com.liu.ai.service.IEmbeddingService;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@RequiredArgsConstructor
@Component
public class ListenDocToEmbeddingStore {

    private final IEmbeddingService iEmbeddingService;

    @RabbitListener(queues = MqConstant.QUEUE_DOCUMENT_MILVUS)
    public void getDoc(DocumentMqMsgDTO document) {
        log.info("开始向量存储...");
        if (StringUtils.isBlank(document.getContentText())) {
            log.info("文档内容为空，不进行嵌入存储");
            throw new AmqpRejectAndDontRequeueException("无效消息");
        }
        try {
            iEmbeddingService.saveDocToMilvus(document);
        } catch (Exception e) {
            log.error("文档嵌入存储失败", e);
        }
    }
}