package com.yourname.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 历史对话存储表
 * </p>
 *
 * @author liujiaxuan
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mind_chat_history")
public class ChatHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id+对话id
     */
    @TableField("memory_id")
    private String memoryId;

    /**
     * 消息类型
     */
    @TableField("message_type")
    private String messageType;

    /**
     * 消息内容
     */
    @TableField("content")
    private String content;

    /**
     * 时间戳
     */
    @TableField("create_time")
    private Long createTime;

    /**
     * 可读的时间戳格式
     */
    @TableField("create_time_timestamp")
    private LocalDateTime createTimeTimestamp;


}
