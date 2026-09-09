package com.liu.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.liu.user.enumsPack.UserStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 
 * @TableName mind_user
 */
@TableName(value ="mind_user")
@Data
public class User {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     用户名
     */
    private String username;

    /**
     * 
     */
    private String password;

    /**
     * 
     */
    private String email;

    /**
     昵称
     */
    private String nickname;

    /**
     头像的url
     */
    private String avatar;

    /**
     * 
     */
    private String phone;

    /**
     用户状态，利用枚举定义
     */
    private UserStatus status;

    /**
     * 
     */
    private LocalDateTime lastLoginTime;

    /**
     * 
     */
    private LocalDateTime createTime;

    /**
     * 
     */
    private LocalDateTime updateTime;

    // 注：原手写 hashCode() 已删除。
    // 类上已有 @Data，Lombok 会同时生成符合约定的 equals() 与 hashCode()（字段集合一致），
    // 避免手写 hashCode 与 Lombok equals 字段不一致导致的契约违反及编译警告。
}