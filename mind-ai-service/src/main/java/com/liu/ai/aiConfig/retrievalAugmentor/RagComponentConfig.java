package com.liu.ai.aiConfig.retrievalAugmentor;

import com.liu.ai.common.UserContext;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
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
            Long userId = UserContext.getUserId();
            log.info("====================> 当前检索器获取到的 userId：{}", userId);
            if(userId == null){
                log.warn("⚠️ userId为空，返回null过滤器");
                return new IsEqualTo("userId", "");
            }
            String id = userId.toString();
            Filter filter = new IsEqualTo("userId", id);
            log.info("====================> 过滤器条件：userId = {}", id);
            return filter;
        };

        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .dynamicFilter(userIdFilter)
                .maxResults(3)
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
                .metadataKeysToInclude(List.of("title"))
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