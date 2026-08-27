package com.liu.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @TableName mind_permission
 */
@TableName("mind_permission")
@Data
public class Permission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 权限编码，对接 Sa-Token hasPermission
     */
    private String permCode;

    private String permName;

    private String description;

    /**
     * 1 启用 0 停用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
