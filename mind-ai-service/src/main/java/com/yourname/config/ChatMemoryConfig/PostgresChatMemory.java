package com.yourname.config.ChatMemoryConfig;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yourname.domain.entity.ChatHistory;
import com.yourname.mapper.MindChatHistoryMapper;
import dev.langchain4j.data.message.*;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.Data;
import org.springframework.stereotype.Component;
import java.util.List;

@Data
@Component
public class PostgresChatMemory implements ChatMemoryStore {

    private final MindChatHistoryMapper mindChatHistoryMapper;


    @Override
    public List<ChatMessage> getMessages(Object memory) {
        String memoryID = String.valueOf(memory);
        LambdaQueryWrapper<ChatHistory> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ChatHistory::getMemoryId, memoryID);
        lqw.orderByAsc(ChatHistory::getCreateTime);
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
    public void updateMessages(Object o, List<ChatMessage> list) {
        String memoryID = String.valueOf(o);
        //TODO
    }

    @Override
    public void deleteMessages(Object o) {
        String memoryID = String.valueOf(o);
        mindChatHistoryMapper.deleteById(memoryID);
    }

    public ChatMessage toChatMessage(ChatHistory chatHistory) throws Exception {
        ChatMessageType type = ChatMessageType.valueOf(chatHistory.getMessageType());
        return switch (type) {
            case ChatMessageType.USER -> UserMessage.userMessage(chatHistory.getContent());
            case ChatMessageType.AI -> AiMessage.aiMessage(chatHistory.getContent());
            case ChatMessageType.SYSTEM -> SystemMessage.systemMessage(chatHistory.getContent());
            default -> throw new Exception("无法解析内容");
        };
    }
}
