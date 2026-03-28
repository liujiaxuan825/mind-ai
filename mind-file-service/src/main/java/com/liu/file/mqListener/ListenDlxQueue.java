package com.liu.file.mqListener;

import com.liu.common.common.constant.MqConstant;
import com.liu.file.domain.Entity.Document;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ListenDlxQueue {

    @RabbitListener(queues = MqConstant.QUEUE_DOCUMENT_PARSE_DLX)
    public void listenDlxQueue(Document documentRecord) {
        log.info("收到死信队列消息: {}", documentRecord);
        //TODO 处理死信队列消息，例如重试解析或记录日志
    }
}
