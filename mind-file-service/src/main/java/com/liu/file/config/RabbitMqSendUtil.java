package com.liu.file.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMqSendUtil implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        String docId = correlationData != null ? correlationData.getId() : "未知ID";
        if (b) {
            log.info("✅ MQ接收成功，文档ID: {}", docId);
        } else {
            log.error("❌ MQ接收失败！文档ID:{}，原因:{}", docId, s);
        }
    }

    @Override
    public void returnedMessage(ReturnedMessage returned) {
        String msgId = returned.getMessage().getMessageProperties().getMessageId();
        String ex = returned.getExchange();
        String key = returned.getRoutingKey();
        String text = returned.getReplyText();

        log.error("❌ 消息路由失败！无法进入队列！docId:{}，exchange:{}，route:{}，原因:{}",
                msgId, ex, key, text);
    }

    public <T> void sendMsg(String exchange, String routingKey, T t, CorrelationData correlationData) {
        try {
            Message message = MessageBuilder.withBody(objectMapper.writeValueAsBytes(t))
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();
            rabbitTemplate.send(exchange, routingKey, message, correlationData);
            log.info("[✅ RabbitMQ] 消息发送成功 | 交换机:{} | 路由键:{}", exchange, routingKey);
        } catch (JsonProcessingException e) {
            log.error("[❌ RabbitMQ] 消息序列化失败", e);
            throw new RuntimeException(e);
        }
    }
}
