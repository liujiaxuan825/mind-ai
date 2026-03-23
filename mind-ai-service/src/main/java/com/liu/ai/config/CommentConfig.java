package com.liu.ai.config;

import com.liu.ai.config.AiServiceConfig.ChatAssistant;
import com.liu.ai.config.ChatMemoryConfig.PostgresChatMemory;
import com.liu.ai.config.ToolConfig.CountTools;
import com.liu.ai.config.guardrailsConfig.InputGuardrails;
import com.liu.ai.config.guardrailsConfig.OutputGuardrails;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CommentConfig {

    @Resource
    private final StreamingChatModel  streamingChatModel;

    @Resource
    private final PostgresChatMemory postgresChatMemory;

    @Resource
    private final RetrievalAugmentor retrievalAugmentor;

    @Resource
    private final InputGuardrails inputGuardrails;

    @Resource
    private final OutputGuardrails outputGuardrails;

    @Bean
    public ChatAssistant creatAiService(){
        return AiServices.builder(ChatAssistant.class)
                .streamingChatModel(streamingChatModel)
                .tools(new CountTools())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(10)
                        .chatMemoryStore(postgresChatMemory)
                        .build())
                .retrievalAugmentor(retrievalAugmentor)
                .inputGuardrails(inputGuardrails)
                .outputGuardrails(outputGuardrails).build();
    }
}
