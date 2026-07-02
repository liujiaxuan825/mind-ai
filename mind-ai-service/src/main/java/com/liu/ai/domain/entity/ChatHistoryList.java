package com.liu.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mind_history_list")
public class ChatHistoryList implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("windows_id")
    private String windowsId;

    @TableField("user_id")
    private Long userId;

    @TableField("content")
    private String content;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_time_timestamp")
    private Long createTimeTimestamp;

    @TableField("update_time_timestamp")
    private Long updateTimeTimestamp;

    @TableField("is_deleted")
    private Integer isDeleted;

    @TableField("is_named")
    private Integer isNamed;

    @TableField("message_count")
    private Integer messageCount;
}
