package com.liu.ai.service.Impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.ai.common.Result;
import com.liu.ai.domain.entity.ChatHistory;
import com.liu.ai.domain.vo.ChatHistoryVO;
import com.liu.ai.mapper.MindChatHistoryMapper;
import com.liu.ai.service.IMindChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 历史对话存储表 服务实现类
 * </p>
 *
 * @author liujiaxuan
 * @since 2026-03-03
 */
@Service
@RequiredArgsConstructor
public class MindChatHistoryServiceImpl extends ServiceImpl<MindChatHistoryMapper, ChatHistory> implements IMindChatHistoryService {

    private final MindChatHistoryMapper mindChatHistoryMapper;

    @Override
    public Result<List<ChatHistoryVO>> getChatHistory(String memory) {
        LambdaQueryWrapper<ChatHistory> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ChatHistory::getMemoryId, memory);
        lqw.orderByAsc(ChatHistory::getCreateTimeTimestamp);
        List<ChatHistory> chatHistoryList = mindChatHistoryMapper.selectList(lqw);
        return Result.success(chatHistoryList.stream().map(item -> BeanUtil.copyProperties(item, ChatHistoryVO.class)).toList());
    }
}
