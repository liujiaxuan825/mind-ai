package com.liu.file.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liu.common.common.constant.MqConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqSendConfig {

    // ====================== 主业务交换机 ======================
    @Bean
    public TopicExchange documentExchange() {
        return ExchangeBuilder.topicExchange(MqConstant.EXCHANGE_DOCUMENT_PARSE_ES_MILVUS)
                .durable(true)
                .build();
    }

    // ====================== 文档解析队列 ======================
    @Bean
    public Queue documentParseQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_DOCUMENT_PARSE)
                .deadLetterExchange(MqConstant.EXCHANGE_DOCUMENT_PARSE_DLX)
                .deadLetterRoutingKey(MqConstant.ROUT_KEY_DOCUMENT_PARSE_DLX)
                .build();
    }

    @Bean
    public Binding documentParseBinding() {
        return BindingBuilder.bind(documentParseQueue())
                .to(documentExchange())
                .with(MqConstant.ROUT_KEY_DOCUMENT_PARSE);
    }

    // ====================== 文档保存队列 ======================
    @Bean
    public Queue documentSaveQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_DOCUMENT_SAVE)
                .deadLetterExchange(MqConstant.EXCHANGE_DOCUMENT_SAVE_DLX)
                .deadLetterRoutingKey(MqConstant.ROUT_KEY_DOCUMENT_SAVE_DLX)
                .build();
    }

    @Bean
    public Binding documentSaveBinding() {
        return BindingBuilder.bind(documentSaveQueue())
                .to(documentExchange())
                .with(MqConstant.ROUT_KEY_DOCUMENT_SAVE);
    }

    // ====================== 解析死信队列 ======================
    @Bean
    public Queue dlxQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_DOCUMENT_PARSE_DLX).build();
    }

    @Bean
    public TopicExchange dlxExchange() {
        return ExchangeBuilder.topicExchange(MqConstant.EXCHANGE_DOCUMENT_PARSE_DLX)
                .durable(true)
                .build();
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue())
                .to(dlxExchange())
                .with(MqConstant.ROUT_KEY_DOCUMENT_PARSE_DLX);
    }

    // ====================== 保存死信队列 ======================
    @Bean
    public Queue dlxDocumentSaveQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_DOCUMENT_SAVE_DLX).build();
    }

    @Bean
    public TopicExchange dlxDocumentSaveExchange() {
        return ExchangeBuilder.topicExchange(MqConstant.EXCHANGE_DOCUMENT_SAVE_DLX)
                .durable(true)
                .build();
    }

    @Bean
    public Binding dlxDocumentSaveBinding() {
        return BindingBuilder.bind(dlxDocumentSaveQueue())
                .to(dlxDocumentSaveExchange())
                .with(MqConstant.ROUT_KEY_DOCUMENT_SAVE_DLX);
    }

    // ====================== Milvus 队列 ======================
    @Bean
    public Queue documentMilvusQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_DOCUMENT_MILVUS).build();
    }

    @Bean
    public Binding documentMilvusBinding() {
        return BindingBuilder.bind(documentMilvusQueue())
                .to(documentExchange())
                .with(MqConstant.ROUT_KEY_DOCUMENT_MILVUS);
    }

    // ====================== Redis 缓存删除 ======================
    @Bean
    public Queue documentRedisCacheQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_DOCUMENT_REDIS_CACHE_DELETE).build();
    }

    @Bean
    public TopicExchange documentRedisCacheExchange() {
        return ExchangeBuilder.topicExchange(MqConstant.EXCHANGE_DOCUMENT_REDIS_CACHE_DELETE)
                .durable(true)
                .build();
    }

    @Bean
    public Binding documentRedisCacheBinding() {
        return BindingBuilder.bind(documentRedisCacheQueue())
                .to(documentRedisCacheExchange())
                .with(MqConstant.ROUT_KEY_DOCUMENT_REDIS_CACHE_DELETE);
    }

    // ====================== 删除流程交换机 ======================
    @Bean
    public TopicExchange documentDeleteExchange() {
        return ExchangeBuilder.topicExchange(MqConstant.EXCHANGE_DOCUMENT_ES_MILVUS_OSS_DELETE)
                .durable(true)
                .build();
    }

    @Bean
    public TopicExchange documentDeleteDlxExchange() {
        return ExchangeBuilder.topicExchange(MqConstant.EXCHANGE_DOCUMENT_ES_MILVUS_OSS_DELETE_DLX)
                .durable(true)
                .build();
    }

    // ====================== ES 删除队列 ======================
    @Bean
    public Queue documentEsDeleteQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_DOCUMENT_ES_DELETE)
                .deadLetterExchange(MqConstant.EXCHANGE_DOCUMENT_ES_MILVUS_OSS_DELETE_DLX)
                .deadLetterRoutingKey(MqConstant.ROUT_KEY_DOCUMENT_ES_DELETE_DLX)
                .build();
    }

    @Bean
    public Binding documentEsDeleteBinding() {
        return BindingBuilder.bind(documentEsDeleteQueue())
                .to(documentDeleteExchange())
                .with(MqConstant.ROUT_KEY_DOCUMENT_ES_DELETE);
    }

    @Bean
    public Queue documentEsDeleteDlxQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_DOCUMENT_ES_DELETE_DLX).build();
    }

    @Bean
    public Binding documentEsDeleteDlxBinding() {
        return BindingBuilder.bind(documentEsDeleteDlxQueue())
                .to(documentDeleteDlxExchange())
                .with(MqConstant.ROUT_KEY_DOCUMENT_ES_DELETE_DLX);
    }

    // ====================== Milvus 删除队列 ======================
    @Bean
    public Queue documentMilvusDeleteQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_DOCUMENT_MILVUS_DELETE)
                .deadLetterExchange(MqConstant.EXCHANGE_DOCUMENT_ES_MILVUS_OSS_DELETE_DLX)
                .deadLetterRoutingKey(MqConstant.ROUT_KEY_DOCUMENT_MILVUS_DELETE_DLX)
                .build();
    }

    @Bean
    public Binding documentMilvusDeleteBinding() {
        return BindingBuilder.bind(documentMilvusDeleteQueue())
                .to(documentDeleteExchange())
                .with(MqConstant.ROUT_KEY_DOCUMENT_MILVUS_DELETE);
    }

    @Bean
    public Queue documentMilvusDeleteDlxQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_DOCUMENT_MILVUS_DELETE_DLX).build();
    }

    @Bean
    public Binding documentMilvusDeleteDlxBinding() {
        return BindingBuilder.bind(documentMilvusDeleteDlxQueue())
                .to(documentDeleteDlxExchange())
                .with(MqConstant.ROUT_KEY_DOCUMENT_MILVUS_DELETE_DLX);
    }

    // ====================== OSS 删除队列 ======================
    @Bean
    public Queue documentOssDeleteQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_DOCUMENT_OSS_DELETE)
                .deadLetterExchange(MqConstant.EXCHANGE_DOCUMENT_ES_MILVUS_OSS_DELETE_DLX)
                .deadLetterRoutingKey(MqConstant.ROUT_KEY_DOCUMENT_OSS_DELETE_DLX)
                .build();
    }

    @Bean
    public Binding documentOssDeleteBinding() {
        return BindingBuilder.bind(documentOssDeleteQueue())
                .to(documentDeleteExchange())
                .with(MqConstant.ROUT_KEY_DOCUMENT_OSS_DELETE);
    }

    @Bean
    public Queue documentOssDeleteDlxQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_DOCUMENT_OSS_DELETE_DLX).build();
    }

    @Bean
    public Binding documentOssDeleteDlxBinding() {
        return BindingBuilder.bind(documentOssDeleteDlxQueue())
                .to(documentDeleteDlxExchange())
                .with(MqConstant.ROUT_KEY_DOCUMENT_OSS_DELETE_DLX);
    }

    // ====================== JSON 消息转换器 ======================
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}