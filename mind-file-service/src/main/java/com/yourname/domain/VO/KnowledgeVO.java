package com.yourname.domain.VO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeVO {
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 知识仓库头像
     */
    private String coverUrl;

    /**
     * 知识库简单描述
     */
    private String description;

    /**
     * 仓库的权限，公开？自己？共享？
     */
    private String filter;

    /**
     * 文档数量
     */
    private Integer documentCount;

    /**
     * 最后一次更新时间
     */
    private LocalDateTime updatedAt;
}
