package com.liu.ai.aiConfig;

import com.liu.ai.aiConfig.AiServiceConfig.ChatTitle;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatTitleCreate {

    private final ChatModel chatModel;


    @Bean
    public ChatTitle chatTitle() {
        return AiServices.builder(ChatTitle.class)
                .chatModel(chatModel)
                .build();
    }

}
