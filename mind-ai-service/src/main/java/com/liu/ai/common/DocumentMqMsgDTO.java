package com.liu.ai.common;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentMqMsgDTO {

    private Long id;

    /**
     * 绑定的知识库
     */
    private Long knowledgeId;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件的路径
     */
    private String fileKey;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件的MIME类型（如：application/pdf）
     */
    private String mimeType;

    /**
     * 文件扩展名（如：pdf、docx）
     */
    private String fileExtension;


    /**
     * 解析出的纯文本内容
     */
    private String contentText;

    /**
     * 文档页数
     */
    private Integer pageCount;

    /**
     * 谁上传的
     */
    private Long createdByUserId;

    /**
     * 是否删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 最后一次更新时间
     */
    private LocalDateTime updatedTime;
}