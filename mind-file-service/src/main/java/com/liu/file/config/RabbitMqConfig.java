package com.liu.file.config;

import com.liu.common.common.constant.MqConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public Queue documentParseQueue() {
        return QueueBuilder.durable(MqConstant.QUEUE_DOCUMENT_PARSE)
                .deadLetterExchange(MqConstant.EXCHANGE_DOCUMENT_PARSE_DLX)
                .deadLetterRoutingKey(MqConstant.ROUT_KEY_DOCUMENT_PARSE_DLX)
                .build();
    }

    @Bean
    public TopicExchange documentParseExchange() {
        return new TopicExchange(MqConstant.EXCHANGE_DOCUMENT_PARSE, true, false);
    }

    @Bean
    public Binding documentParseBinding() {
        return BindingBuilder.bind(documentParseQueue()).to(documentParseExchange()).with(MqConstant.ROUT_KEY_DOCUMENT_PARSE);
    }

    @Bean
    public Queue dlxQueue() {
        return new Queue(MqConstant.QUEUE_DOCUMENT_PARSE_DLX, true, false, false);
    }

    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(MqConstant.EXCHANGE_DOCUMENT_PARSE_DLX, true, false);
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(MqConstant.ROUT_KEY_DOCUMENT_PARSE_DLX);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
