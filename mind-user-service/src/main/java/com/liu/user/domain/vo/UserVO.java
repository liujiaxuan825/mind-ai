package com.liu.user.domain.vo;

import com.liu.user.enumsPack.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserVO {
    private Long id;

    /**
     用户名
     */
    private String username;

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
     * 当前用户拥有的角色编码，前端用是否包含 ADMIN 控制菜单。
     */
    private List<String> roleCodes;
}
