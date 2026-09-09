package com.liu.user.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 登录成功即返回用户的菜单树（动态侧边栏）。
     * 前端可直接使用该字段渲染，或通过 /menu/mine 再次拉取。
     */
    private List<MenuVO> menus;

}
