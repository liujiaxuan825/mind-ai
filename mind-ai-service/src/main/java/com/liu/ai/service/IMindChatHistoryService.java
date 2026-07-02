package com.liu.ai.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.ai.common.Result;
import com.liu.ai.domain.entity.ChatHistory;
import com.liu.ai.domain.vo.ChatHistoryVO;

import java.util.List;

/**
 * <p>
 * 历史对话存储表 服务类
 * </p>
 *
 * @author liujiaxuan
 * @since 2026-03-03
 */
public interface IMindChatHistoryService extends IService<ChatHistory> {

    Result<List<ChatHistoryVO>> getChatHistory(String memory);
}
