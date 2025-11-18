package com.yourname.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserLoginVO {
    private String token;


    private String tokenType = "Bearer";


    private Long expiresIn;

    /**
     * 具体的过期时间点
     */
    private LocalDateTime expiresAt;

    /**
     * 用户信息
     */
    private UserVO userInfo;

    /**
     * 是否首次登录
     */
    private Boolean firstLogin = false;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

}
