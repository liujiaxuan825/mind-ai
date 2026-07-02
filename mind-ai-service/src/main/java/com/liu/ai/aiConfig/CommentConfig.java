package com.liu.ai.aiConfig;

import com.liu.ai.aiConfig.AiServiceConfig.ChatAssistant;
import com.liu.ai.aiConfig.ChatMemoryConfig.PostgresChatMemory;
import com.liu.ai.aiConfig.ToolConfig.InternalDocsTools;
import com.liu.ai.aiConfig.guardrailsConfig.InputGuardrails;
import com.liu.ai.aiConfig.guardrailsConfig.OutputGuardrails;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CommentConfig {

    private final StreamingChatModel chatModel;

    private final PostgresChatMemory postgresChatMemory;

    private final RetrievalAugmentor retrievalAugmentor;

    private final InputGuardrails inputGuardrails;

    private final OutputGuardrails outputGuardrails;

    @Bean
    public ChatAssistant creatAiService(){
        return AiServices.builder(ChatAssistant.class)
                .streamingChatModel(chatModel)
                .tools(new InternalDocsTools())
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(5)
                        .chatMemoryStore(postgresChatMemory)
                        .build())
                .retrievalAugmentor(retrievalAugmentor)
                .inputGuardrails(inputGuardrails)
                .outputGuardrails(outputGuardrails).build();
    }
}