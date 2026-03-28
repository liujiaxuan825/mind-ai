package com.liu.search.mqListener;

import com.liu.file.Service.IMindDocumentService;
import com.liu.search.config.DocumentToEsConverter;
import com.liu.search.config.EsDocumentRepository;
import com.liu.file.domain.Entity.Document;
import com.liu.search.domain.Entity.EsDocument;
import com.liu.common.common.constant.MqConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@RequiredArgsConstructor
public class ListenDocSave {

    private final IMindDocumentService mindDocumentService;

    private final EsDocumentRepository esDocumentRepository;

    private final DocumentToEsConverter documentToEsConverter;

    @RabbitListener(
            bindings = @QueueBinding(
                value = @Queue(name = MqConstant.QUEUE_DOCUMENT_SAVE),
                exchange = @Exchange(name = MqConstant.EXCHANGE_DOCUMENT_SAVE,type = ExchangeTypes.TOPIC),
                key = {MqConstant.ROUT_KEY_DOCUMENT_SAVE}
            )
    )
    public void saveDcoToEs(Long documentId) {
        Document document = mindDocumentService.getById(documentId);
        EsDocument esDocument = documentToEsConverter.convertDocumentToEsDocument(document);
        esDocumentRepository.save(esDocument);
    }
}
