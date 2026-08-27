package com.liu.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @TableName mind_role_permission
 */
@TableName("mind_role_permission")
@Data
public class RolePermission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long roleId;

    private Long permissionId;

    private LocalDateTime createTime;

    public RolePermission(Long roleId, Long permissionId, LocalDateTime now) {
        this.roleId = roleId;
        this.permissionId = permissionId;
        this.createTime = now;
    }
}
