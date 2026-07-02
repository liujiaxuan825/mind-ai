package com.liu.ai.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.ai.domain.entity.ChatHistory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 历史对话存储表 Mapper 接口
 * </p>
 *
 * @author liujiaxuan
 * @since 2026-03-03
 */
@Mapper
public interface MindChatHistoryMapper extends BaseMapper<ChatHistory> {

}
