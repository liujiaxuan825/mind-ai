package com.yourname.mqListener;

import com.yourname.domain.Entity.Document;
import com.yourname.mind.common.constant.MqConstant;
import com.yourname.service.IParseService;
import com.yourname.service.IUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@RequiredArgsConstructor
public class ListenDocParse {

    private final IParseService iParseService;


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
        iParseService.DocParse(documentRecord);
    }

}
