package com.liu.ai.service.Impl;

import com.liu.ai.common.DocumentMqMsgDTO;
import com.liu.ai.service.IEmbeddingService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements IEmbeddingService {

    private final EmbeddingStoreIngestor ingestor;

    @Override
    public void saveDocToMilvus(DocumentMqMsgDTO document) {
        Document embeddingDocument = Document.document(document.getContentText());
        Metadata metadata = embeddingDocument.metadata();
        metadata.put("documentId", String.valueOf(document.getId()));
        metadata.put("title", document.getName());
        metadata.put("userId", String.valueOf(document.getCreatedByUserId()));
        metadata.put("knowledgeId", String.valueOf(document.getKnowledgeId()));
        metadata.put("createdTime", document.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        ingestor.ingest(embeddingDocument);
    }
}
