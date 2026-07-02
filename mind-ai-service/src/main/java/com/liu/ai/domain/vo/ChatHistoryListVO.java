package com.liu.ai.domain.vo;

import lombok.Data;

@Data
public class ChatHistoryListVO {
    private Long id;

    private String windowsId;

    private Long userId;

    private String content;

    private Long createTimeTimestamp;

    private Long updateTimeTimestamp;
}
