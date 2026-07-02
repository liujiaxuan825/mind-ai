package com.liu.ai.aiConfig.ToolConfig;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class InternalDocsTools {

    @Tool(value = "")
    public void searchInternalDocs(String question) {

    }
}
