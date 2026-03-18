package com.yourname.config.RagConfig;

import com.yourname.mind.config.UserContextHolder;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.val;
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
                    String userId = String.valueOf(UserContextHolder.getCurrentUserId());
                    Metadata metadata = doc.metadata();
                    metadata.put("userId", userId);
                    String text = doc.text().replaceAll("\\n{3,}", "\n\n");
                    return Document.from(text, metadata);
                })
                .documentSplitter(DocumentSplitters.recursive(500, 100))
                .textSegmentTransformer(seg ->{
                    String filename = seg.metadata().getString("file_name");
                    String userId = seg.metadata().getString("user_id");
                    String text = "[来源文档：" + filename + "]（所属用户：" + userId + "）\n" + seg.text();
                    return TextSegment.from(text, seg.metadata());
                })
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }
}
