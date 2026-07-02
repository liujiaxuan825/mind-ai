package com.liu.ai.aiConfig.ChatMemoryConfig;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liu.ai.domain.entity.ChatHistory;
import com.liu.ai.mapper.MindChatHistoryMapper;
import dev.langchain4j.data.message.*;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Component
@Slf4j
public class PostgresChatMemory implements ChatMemoryStore {

    private final MindChatHistoryMapper mindChatHistoryMapper;


    @Override
    public List<ChatMessage> getMessages(Object memory) {
        String memoryID = String.valueOf(memory);
        LambdaQueryWrapper<ChatHistory> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ChatHistory::getMemoryId, memoryID);
        lqw.in(ChatHistory::getMessageType, "USER", "AI");
        lqw.orderByAsc(ChatHistory::getCreateTimeTimestamp);
        List<ChatHistory> chatList = mindChatHistoryMapper.selectList(lqw);
        return chatList.stream().map(item -> {
            try {
                return toChatMessage(item);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMessages(Object o, List<ChatMessage> newMessages) {
        String memoryID = String.valueOf(o);
        List<ChatHistory> existMessages = getExistMessages(memoryID);
        int existSize = existMessages.size();
        if(newMessages.size() <= existSize) {
            return ;
        }

        List<ChatMessage> newMessagesList = newMessages.subList(existSize, newMessages.size());
        List<ChatHistory> insertList = newMessagesList.stream()
                .filter(msg -> !(msg instanceof SystemMessage))
                .map(msg -> buildChatHistory(memoryID, msg))
                .toList();
        mindChatHistoryMapper.insertOrUpdate(insertList);
    }

    private ChatHistory buildChatHistory(String memoryID, ChatMessage msg) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setMemoryId(memoryID);
        chatHistory.setMessageType(getMessageType(msg));
        chatHistory.setContent(getMessageContent(msg));
        chatHistory.setWindowsId(parseWindowId(memoryID));
        chatHistory.setCreateTime(LocalDateTime.now());
        chatHistory.setCreateTimeTimestamp(System.currentTimeMillis());
        return chatHistory;
    }

    private String parseWindowId(String memoryId) {
        int index = memoryId.indexOf("_");
        if (index != -1) {
            return memoryId.substring(index + 1);
        }
        return memoryId;
    }

    private String getMessageContent(ChatMessage msg) {
        try {
            if (msg instanceof UserMessage m) return m.singleText();
            if (msg instanceof AiMessage m) return m.text() == null ? "" : m.text();
            return "";
        } catch (Exception e) {
            log.error("获取消息内容失败", e);
            return "";
        }
    }

    private String getMessageType(ChatMessage msg) {
        if (msg instanceof UserMessage) return "USER";
        if (msg instanceof AiMessage) return "AI";
        if (msg instanceof SystemMessage) return "SYSTEM";
        throw new IllegalArgumentException("不支持的消息类型");
    }

    private List<ChatHistory> getExistMessages(String memoryID) {
        LambdaQueryWrapper<ChatHistory> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ChatHistory::getMemoryId, memoryID);
        lqw.in(ChatHistory::getMessageType, "USER", "AI");
        lqw.orderByAsc(ChatHistory::getCreateTimeTimestamp);
        return mindChatHistoryMapper.selectList(lqw);
    }

    @Override
    public void deleteMessages(Object o) {
        String memoryID = String.valueOf(o);
        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getMemoryId, memoryID);
        mindChatHistoryMapper.delete(wrapper);
    }

    private ChatMessage toChatMessage(ChatHistory chatHistory) {
        try {
            return switch (chatHistory.getMessageType()) {
                case "USER" -> UserMessage.userMessage(chatHistory.getContent());
                case "AI" -> AiMessage.aiMessage(chatHistory.getContent());
                default -> UserMessage.userMessage("[未知消息]");
            };
        } catch (Exception e) {
            log.error("解析消息失败 ID:{}", chatHistory.getId(), e);
            return UserMessage.userMessage("[解析失败]");
        }
    }
}