package com.liu.ai.controller;


import com.liu.ai.aiConfig.AiServiceConfig.ChatAssistant;
import com.liu.common.untils.UserContext;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 历史对话存储表 前端控制器
 * </p>
 *
 * @author liujiaxuan
 * @since 2026-03-03
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class MindChatHistoryController {

    private final ChatAssistant chatAssistant;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public TokenStream chat(@RequestParam("windowsId") String windowsId, @RequestParam("ask") String ask){
        String memory = UserContext.getUserId().toString() + "_" + windowsId;
        return chatAssistant.chat(memory, ask);
    }
}
