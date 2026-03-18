package com.yourname.config.ToolConfig;

import dev.langchain4j.agent.tool.Tool;

public class CountTools {

    @Tool("")
    Integer countConcerned(){
        return 1;
    }
}
