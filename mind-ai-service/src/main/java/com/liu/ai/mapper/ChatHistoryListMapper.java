package com.liu.ai.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.ai.domain.entity.ChatHistoryList;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 历史对话列表 Mapper 接口
 * </p>
 *
 * @author liujiaxuan
 * @since 2026-03-03
 */
@Mapper
public interface ChatHistoryListMapper extends BaseMapper<ChatHistoryList> {

}