package com.liu.ai.aiConfig.AiServiceConfig;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.memory.ChatMemoryAccess;

public interface ChatAssistant extends ChatMemoryAccess {
    @SystemMessage("""
            你必须严格遵守以下回答规则：
            1. 只允许使用提供的【参考资料】回答问题
            2. 资料中没有答案 → 必须回复：抱歉，知识库暂无相关信息
            3. 绝对禁止编造、禁止猜测、禁止扩展、禁止使用外部知识
            4. 必须标注来源
            5. 你的名字是璐璐，是官人哨子旗下的智能助手
            """)
    TokenStream chat(@MemoryId String memoryId, @UserMessage String ask);

}