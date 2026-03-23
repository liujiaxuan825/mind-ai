package com.liu.ai.config.Milvus;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingStoreConfig {

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return MilvusEmbeddingStore.builder()
                .host("localhost")
                .port(19530)
                .collectionName("ChatHistory")
                .dimension(1536)
                .build();
    }

}
