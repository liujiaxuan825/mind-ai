package com.liu.ai.service.Impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.ai.aiConfig.AiServiceConfig.ChatTitle;
import com.liu.ai.common.Result;
import com.liu.ai.common.UserContext;
import com.liu.ai.domain.entity.ChatHistory;
import com.liu.ai.domain.entity.ChatHistoryList;
import com.liu.ai.domain.vo.ChatHistoryListVO;
import com.liu.ai.mapper.ChatHistoryListMapper;
import com.liu.ai.mapper.MindChatHistoryMapper;
import com.liu.ai.service.IChatHistoryListService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 历史对话列表 服务实现类
 * </p>
 *
 * @author liujiaxuan
 * @since 2026-03-03
 */
@Service
@RequiredArgsConstructor
public class ChatHistoryListServiceImpl extends ServiceImpl<ChatHistoryListMapper, ChatHistoryList> implements IChatHistoryListService {

    private final ChatHistoryListMapper chatHistoryListMapper;

    private final MindChatHistoryMapper mindChatHistoryMapper;

    private final ChatTitle chatTitle;



    @Override
    public Result<List<ChatHistoryListVO>> getChatHistoryList() {
        LambdaQueryWrapper<ChatHistoryList> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ChatHistoryList::getUserId, UserContext.getUserId());
        lqw.eq(ChatHistoryList::getIsDeleted, 0);
        lqw.orderByDesc(ChatHistoryList::getUpdateTimeTimestamp);
        List<ChatHistoryList> chatHistoryList = chatHistoryListMapper.selectList(lqw);
        return Result.success(chatHistoryList.stream().map(item -> BeanUtil.copyProperties(item, ChatHistoryListVO.class)).toList());
    }

    @Override
    public Result<Void> createNewChatWindow(String windowsId) {
        ChatHistoryList chatHistoryList = new ChatHistoryList();
        chatHistoryList.setUserId(UserContext.getUserId());
        chatHistoryList.setWindowsId(windowsId);
        chatHistoryList.setContent("新对话");
        chatHistoryList.setCreateTime(LocalDateTime.now());
        chatHistoryList.setCreateTimeTimestamp(System.currentTimeMillis());
        chatHistoryList.setUpdateTimeTimestamp(System.currentTimeMillis());
        chatHistoryList.setIsDeleted(0);
        chatHistoryList.setIsNamed(0);
        chatHistoryList.setMessageCount(0);
        chatHistoryListMapper.insert(chatHistoryList);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteChatWindow(String windowsId) {
        //删除对话窗口
        LambdaUpdateWrapper<ChatHistoryList> listLqw = new LambdaUpdateWrapper<>();
        listLqw.eq(ChatHistoryList::getUserId, UserContext.getUserId());
        listLqw.eq(ChatHistoryList::getWindowsId, windowsId);
        listLqw.set(ChatHistoryList::getIsDeleted, 1);
        chatHistoryListMapper.update(listLqw);
        //删除对话记录
        LambdaQueryWrapper<ChatHistory> hisLqw = new LambdaQueryWrapper<>();
        hisLqw.eq(ChatHistory::getWindowsId, windowsId);
        mindChatHistoryMapper.delete(hisLqw);
        return Result.success();
    }

    @Override
    @Async
    public void autoGenerateWindowTitle(String windowsId, Long userId) {
        try {

            LambdaUpdateWrapper<ChatHistoryList> listLuw = new LambdaUpdateWrapper<>();
            listLuw.eq(ChatHistoryList::getWindowsId, windowsId);
            listLuw.eq(ChatHistoryList::getUserId, userId);
            listLuw.eq(ChatHistoryList::getIsDeleted, 0);
            listLuw.eq(ChatHistoryList::getIsNamed, 0);
            listLuw.setSql("message_count = CASE WHEN message_count < 3 THEN message_count + 1 ELSE message_count END");
            int update = chatHistoryListMapper.update(null, listLuw);

            if (update == 0) {
                return;
            }

            ChatHistoryList one = this.getOne(
                    new LambdaQueryWrapper<ChatHistoryList>()
                            .eq(ChatHistoryList::getWindowsId, windowsId)
                            .eq(ChatHistoryList::getUserId, userId)
                            .eq(ChatHistoryList::getIsDeleted, 0)
                            .eq(ChatHistoryList::getIsNamed, 0)
                            .last("LIMIT 1")
            );

            if (one == null || one.getMessageCount() < 3) {
                return;
            }
            List<ChatHistory> historyList = getHistoryByWindowId(windowsId, userId);
            if (historyList == null || historyList.isEmpty()) {
                return;
            }
            StringBuilder content = new StringBuilder();
            for (ChatHistory chatHistory : historyList) {
                content.append(chatHistory.getMessageType()).append(":").append(chatHistory.getContent()).append("\n");
            }
            String title = chatTitle.createTitle(content.toString());

            updateChatHistoryListTitle(one, title);
        } catch (Exception e) {
            log.error("自动生成标题失败", e);
        }
    }

    private void updateChatHistoryListTitle(ChatHistoryList one, String title) {
        one.setContent(title);
        one.setIsNamed(1);
        chatHistoryListMapper.updateById(one);
    }

    private List<ChatHistory> getHistoryByWindowId(String windowsId, Long userId) {
        LambdaQueryWrapper<ChatHistory> hisLqw = new LambdaQueryWrapper<>();
        String memoryId = userId + "_" + windowsId;
        hisLqw.eq(ChatHistory::getMemoryId, memoryId);
        hisLqw.orderByAsc(ChatHistory::getCreateTimeTimestamp);
        return mindChatHistoryMapper.selectList(hisLqw);
    }
}