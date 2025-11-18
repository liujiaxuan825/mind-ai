package com.yourname.mind.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    DISABLED(0, "禁用", "用户被禁用，无法登录系统"),
    ENABLED(1, "启用", "用户正常状态，可以登录和使用系统"),
    UNACTIVATED(2, "未激活", "用户已注册但未激活，需要邮箱验证"),
    LOCKED(3, "锁定", "用户因多次密码错误被临时锁定");

    private final Integer code;
    private final String description;
    private final String detail;

    UserStatusEnum(Integer code, String description, String detail) {
        this.code = code;
        this.description = description;
        this.detail = detail;
    }

    public static UserStatusEnum getByCode(Integer code) {
        if (code == null) return null;
        for (UserStatusEnum status : values()) {
            if (status.getCode().equals(code)) return status;
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    public static boolean canLogin(Integer code) {
        UserStatusEnum status = getByCode(code);
        return status != null && status == ENABLED;
    }
}