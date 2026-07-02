package com.liu.ai.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.ai.common.Result;
import com.liu.ai.domain.entity.ChatHistoryList;
import com.liu.ai.domain.vo.ChatHistoryListVO;


import java.util.List;

/**
 * <p>
 * 历史对话列表 服务类
 * </p>
 *
 * @author liujiaxuan
 * @since 2026-03-03
 */
public interface IChatHistoryListService extends IService<ChatHistoryList> {

    Result<List<ChatHistoryListVO>> getChatHistoryList();

    Result<Void> createNewChatWindow(String windowsId);

    Result<Void> deleteChatWindow(String windowsId);

    void autoGenerateWindowTitle(String windowsId, Long userId);
}