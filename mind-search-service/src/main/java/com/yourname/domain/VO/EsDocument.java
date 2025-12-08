package com.yourname.domain.VO;

import com.yourname.domain.enumsPack.DocumentStatus;
import lombok.Data;

@Data
public class EsDocument {
    /**
     * 唯一id
     */
    private Long id;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件此时的状态
     */
    private DocumentStatus status;

    /**
     * 摘要，状态正确展示内容的一部分，错误是显示错误信息，其他状态不展示
     */
    private String content;
}
