package com.liu.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @TableName mind_user_role
 */
@TableName("mind_user_role")
@Data
public class UserRole {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long roleId;

    private LocalDateTime createTime;

    public UserRole(Long userId, Long roleId, LocalDateTime now) {
        this.userId = userId;
        this.roleId = roleId;
        this.createTime = now;
    }
}
