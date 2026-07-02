package com.liu.ai.controller;


import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.liu.ai.aiConfig.AiServiceConfig.ChatAssistant;
import com.liu.ai.common.Result;
import com.liu.ai.common.UserContext;
import com.liu.ai.domain.vo.ChatHistoryListVO;
import com.liu.ai.domain.vo.ChatHistoryVO;
import com.liu.ai.service.IMindChatHistoryService;
import com.liu.ai.service.IChatHistoryListService;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * <p>
 * 历史对话存储表 前端控制器
 * </p>
 *
 * @author liujiaxuan
 * @since 2026-03-03
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@RefreshScope
public class MindChatHistoryController {

    private final ChatAssistant chatAssistant;

    private final IMindChatHistoryService mindChatHistoryService;

    private final IChatHistoryListService chatHistoryListService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SentinelResource(value = "user:chat", blockHandler = "chatBlock")
    public SseEmitter chat(@RequestParam("windowsId") String windowsId, @RequestParam("ask") String ask) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            log.warn("⚠️ userId为空");
            return new SseEmitter();
        }
        chatHistoryListService.autoGenerateWindowTitle(windowsId, userId);
        String memory = userId + "_" + windowsId;
        SseEmitter emitter = new SseEmitter();
        chatAssistant.chat(memory, ask)
                .onPartialResponse(token -> {
                    try {
                        emitter.send(token);
                    } catch (Exception ignored) {
                    }
                })
                .onCompleteResponse(complete -> {
                    emitter.complete();
                })
                .onError(emitter::completeWithError)
                .start();
        return emitter;
    }

    @PostMapping("/history")
    public Result<List<ChatHistoryVO>> getChatHistory(@RequestParam("windowsId") String windowsId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("用户未登录");
        }
        String memory = userId + "_" + windowsId;
        return mindChatHistoryService.getChatHistory(memory);
    }

    @PostMapping("/history/list")
    public Result<List<ChatHistoryListVO>> getChatHistoryList() {
        return chatHistoryListService.getChatHistoryList();
    }


    @PostMapping("/create/newChatWindow/{windowsId}")
    public Result<Void> createNewChatWindow(@PathVariable("windowsId") String windowsId) {
        return chatHistoryListService.createNewChatWindow(windowsId);
    }

    @DeleteMapping("/deleteChatWindow/{windowsId}")
    public Result<Void> deleteChatWindow(@PathVariable("windowsId") String windowsId) {
        return chatHistoryListService.deleteChatWindow(windowsId);
    }

    public TokenStream chatBlock(String windowsId, String ask, BlockException e) throws BlockException {
        log.warn("[Sentinel] 聊天接口被拦截 → 规则：{}", e.getRule());
        throw e;
    }

}