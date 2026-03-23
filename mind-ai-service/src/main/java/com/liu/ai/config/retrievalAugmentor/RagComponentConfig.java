package com.liu.ai.config.retrievalAugmentor;

import com.liu.common.mind.config.UserContextHolder;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.cohere.CohereScoringModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Function;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Configuration
@RequiredArgsConstructor
public class RagComponentConfig {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChatModel chatModel;


    @Bean
    public QueryTransformer createQueryTransformer(){
        return CompressingQueryTransformer.builder()
                .chatModel(chatModel)
                .build();
    }


    @Bean
    public ContentRetriever createContentRetriever() {
        Function<Query, Filter> userIdFilter = (query)->{
            Long userId = UserContextHolder.getCurrentUserId();
            if(userId == null){
                return null;
            }
            String id = userId.toString();
            return metadataKey("userId").isEqualTo(id);
        };

        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .dynamicFilter(userIdFilter)
                .maxResults(5)
                .build();
    }

    /*@Bean
    public ContentAggregator createContentAggregator(){
        return ReRankingContentAggregator.builder()
                .scoringModel(CohereScoringModel.builder()
                        .apiKey("")
                        .modelName("")
                        .build())
                .maxResults(3)
                .build();
    }*/

    /*@Bean
    public QueryRouter createQueryRouter(){

        QueryRouter queryRouter = new LanguageModelQueryRouter(chatModel, );
    }*/

    @Bean
    public ContentInjector createContentInjector(){
        return DefaultContentInjector.builder()
                .metadataKeysToInclude(List.of("file_name", "index"))
                .build();
    }

    @Bean
    public RetrievalAugmentor createRetrieval(){
        return DefaultRetrievalAugmentor.builder()
                .queryTransformer(createQueryTransformer())
                .contentInjector(createContentInjector())
                .contentRetriever(createContentRetriever())
                /*.contentAggregator(createContentAggregator())*/
                .build();
    }
}
