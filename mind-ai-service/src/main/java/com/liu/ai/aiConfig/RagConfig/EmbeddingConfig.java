package com.liu.ai.aiConfig.RagConfig;

import com.liu.common.untils.UserContext;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class EmbeddingConfig {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Bean
    public EmbeddingStoreIngestor createEsi(){
        return EmbeddingStoreIngestor.builder()
                .documentTransformer(doc ->{
                    String text = doc.text().replaceAll("\\n{3,}", "\n\n");
                    Metadata metadata = doc.metadata();
                    metadata.put("documentId", metadata.getString("documentId"));
                    metadata.put("title", metadata.getString("title"));
                    metadata.put("userId", metadata.getString("userId"));
                    metadata.put("knowledgeId", metadata.getString("knowledgeId"));
                    metadata.put("createdTime", metadata.getString("createdTime"));
                    return Document.from(text, metadata);
                })
                .documentSplitter(DocumentSplitters.recursive(300, 30))
                .textSegmentTransformer(seg ->{
                    String filename = seg.metadata().getString("title");
                    String userId = seg.metadata().getString("userId");
                    String text = "[来源文档：" + filename + "]（所属用户：" + userId + "）\n" + seg.text();
                    return TextSegment.from(text, seg.metadata());
                })
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }
}
