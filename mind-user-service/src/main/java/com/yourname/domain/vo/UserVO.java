package com.yourname.domain.vo;

import com.yourname.enumsPack.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

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
}
