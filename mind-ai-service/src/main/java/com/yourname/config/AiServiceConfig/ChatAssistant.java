package com.yourname.config.AiServiceConfig;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.memory.ChatMemoryAccess;

public interface ChatAssistant extends ChatMemoryAccess {
    @SystemMessage("你是一个内部知识库系统的助手，你回答问题时要先基于知识库中的文档，知识库中没有的话再用别的回答，不论是基于哪种回答，都要标注出来")
    TokenStream chat(@MemoryId String userId, @UserMessage String ask);
}
