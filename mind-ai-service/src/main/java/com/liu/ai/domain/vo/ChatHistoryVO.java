package com.liu.ai.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatHistoryVO {
    private Long id;

    private String memoryId;

    private String messageType;

    private String content;

    private LocalDateTime createTime;

    private Long createTimeTimestamp;
}