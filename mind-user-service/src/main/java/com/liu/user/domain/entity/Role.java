package com.liu.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @TableName mind_role
 */
@TableName("mind_role")
@Data
public class Role {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 角色编码，对接 Sa-Token hasRole
     */
    private String roleCode;

    private String roleName;

    private String description;

    /**
     * 1 启用 0 停用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
