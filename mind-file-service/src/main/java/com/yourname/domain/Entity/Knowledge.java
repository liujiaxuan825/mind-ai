package com.yourname.domain.Entity;

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
 * 
 * </p>
 *
 * @author liujiaxuan
 * @since 2025-11-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mind_knowledge")
public class Knowledge implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建人
     */
    private Long userId;

    /**
     * 名称
     */
    private String name;

    /**
     * 知识库简单描述
     */
    private String description;

    /**
     * 知识仓库头像
     */
    private String coverUrl;

    /**
     * 仓库的权限，公开？自己？共享？
     */
    private String filter;

    /**
     * 文档数量
     */
    private Integer documentCount;

    /**
     * 已经存储的字节数
     */
    private Long storageUsed;
    

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后一次更新时间
     */
    private LocalDateTime updatedAt;


}
