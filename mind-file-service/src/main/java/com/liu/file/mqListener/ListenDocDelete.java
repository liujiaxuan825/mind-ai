package com.liu.file.mqListener;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.liu.common.common.constant.MqConstant;
import com.liu.common.common.constant.RedisConstant;
import com.liu.common.config.redisConfig.StringRedisTemplateConfig;
import com.liu.file.domain.Entity.Document;
import com.liu.file.domain.VO.DocumentVO;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ListenDocDelete {

    private final StringRedisTemplateConfig.RedisCacheUtils redisCacheUtils;
    private final Cache<String, DocumentVO> documentCache;

    @RabbitListener(queues = MqConstant.QUEUE_DOCUMENT_REDIS_CACHE_DELETE)
    public void documentRedisCacheDelete(@Payload Document document, Channel channel, Message message) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        log.info("最终兜底删除redis和本地缓存，文档id：{}", document.getId());
        try {
            // 删除redis缓存
            String key = RedisConstant.DOCUMENT_ID + document.getCreatedByUserId() + "_" + document.getId();
            redisCacheUtils.delete(key);
            // 删除本地缓存
            documentCache.invalidate(document.getId().toString());
            // 手动确认消息
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("删除redis缓存失败，文档id：{}", document.getId(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
