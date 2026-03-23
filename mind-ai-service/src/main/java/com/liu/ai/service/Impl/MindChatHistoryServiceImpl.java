package com.liu.ai.service.Impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.ai.domain.entity.ChatHistory;
import com.liu.ai.mapper.MindChatHistoryMapper;
import com.liu.ai.service.IMindChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 历史对话存储表 服务实现类
 * </p>
 *
 * @author liujiaxuan
 * @since 2026-03-03
 */
@Service
public class MindChatHistoryServiceImpl extends ServiceImpl<MindChatHistoryMapper, ChatHistory> implements IMindChatHistoryService {

}
