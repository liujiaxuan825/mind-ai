package com.yourname.mqListener;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yourname.Service.IMindDocumentService;
import com.yourname.domain.Entity.Document;
import com.yourname.domain.enumsPack.DocumentStatus;
import com.yourname.mind.aliyun.AliyunOssUtil;
import com.yourname.mind.common.constant.MqConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class ListenDocParse {

    private final IMindDocumentService mindDocumentService;
    private final AliyunOssUtil  aliyunOssUtil;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstant.QUEUE_DOCUMENT_PARSE),
            exchange = @Exchange(name = MqConstant.EXCHANGE_DOCUMENT_PARSE,type = ExchangeTypes.TOPIC),
            key = {MqConstant.ROUT_KEY_DOCUMENT_PARSE}
    )
    )
    public void listenDocParse(Document documentRecord) {
        if (documentRecord == null) {
            return;
        }
        mindDocumentService.DocParse(documentRecord);
    }


}
