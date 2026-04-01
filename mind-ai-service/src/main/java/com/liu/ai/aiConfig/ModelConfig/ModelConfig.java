package com.liu.ai.aiConfig.ModelConfig;

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
                .apiKey("sk-1054e5fe95734a879f619f5059a1867e")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .modelName("qwen-plus")
                .build();
    }

    //向量分割模型
    @Bean
    public EmbeddingModel createEmbeddingModel(){
        return OpenAiEmbeddingModel.builder()
                .apiKey("sk-1054e5fe95734a879f619f5059a1867e")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .modelName("text-embedding-v3")
                .build();
    }

    @Bean ChatModel createQueryModel(){
        return OpenAiChatModel.builder()
                .apiKey("sk-1054e5fe95734a879f619f5059a1867e")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .modelName("qwen-plus")
                .build();
    }

}
