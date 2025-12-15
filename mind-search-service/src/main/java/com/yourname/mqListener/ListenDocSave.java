package com.yourname.mqListener;

import com.yourname.Service.IMindDocumentService;
import com.yourname.config.DocumentToEsConverter;
import com.yourname.config.EsDocumentRepository;
import com.yourname.domain.Entity.Document;
import com.yourname.domain.Entity.EsDocument;
import com.yourname.mind.common.constant.MqConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@RequiredArgsConstructor
public class ListenDocSave {

    /*private final IMindDocumentService mindDocumentService;

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
    }*/
}
