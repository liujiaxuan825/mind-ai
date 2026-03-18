package com.yourname.config.ModelConfig;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelConfig {

    //对话模型
    @Bean
    public StreamingChatModel createChatModel(){
        return OpenAiStreamingChatModel.builder()
                .apiKey("")
                .modelName("")
                .build();
    }

    //向量分割模型
    @Bean
    public EmbeddingModel createEmbeddingModel(){
        return OpenAiEmbeddingModel.builder()
                .apiKey("")
                .modelName("")
                .build();
    }

    @Bean ChatModel createQueryModel(){
        return OpenAiChatModel.builder()
                .apiKey("")
                .modelName("")
                .build();
    }

}
