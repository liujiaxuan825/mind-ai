package com.yourname.domain.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import com.yourname.domain.enumsPack.DocumentStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author liujiaxuan
 * @since 2025-11-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mind_document")
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 绑定的知识库
     */
    private Long KnowledgeId;

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
     * 文件此时的状态
     */
    private DocumentStatus status;

    /**
     * 解析错误信息
     */
    private String parseErrorMessage;

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

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;


}
