package com.liu.ai.aiConfig.AiServiceConfig;

import dev.langchain4j.service.SystemMessage;

public interface ChatTitle {
    @SystemMessage("""
                你是专业标题生成器。
                规则：
                1. 只生成10字以内中文标题
                2. 只返回标题，不要任何其他内容
                3. 简洁、准确、概括主题
            """)
    String createTitle(String content);
}
