package com.yourname.enumsPack;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Data;
import lombok.Getter;

@Getter
public enum UserStatus {
    DISABLE(0,"禁用","用户状态被禁用"),

    ENABLE(1,"启用","用户状态正常"),

    UNACTIVATED(2, "未激活", "用户已注册但未激活，需要邮箱验证"),

    LOCKED(3, "锁定", "用户因多次密码错误被临时锁定");


    @EnumValue
    private final Integer code;
    private final String description;
    private final String detail;

    UserStatus(Integer code, String description, String detail) {
        this.code = code;
        this.description = description;
        this.detail = detail;
    }
    public static UserStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserStatus status : UserStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 检查code是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 检查用户是否可登录
     */
    public static boolean canLogin(Integer code) {
        UserStatus status = getByCode(code);
        return status != null && (status == ENABLE);
    }

    /**
     * 获取所有可用的状态码
     */
    public static Integer[] getAvailableCodes() {
        UserStatus[] values = values();
        Integer[] codes = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            codes[i] = values[i].getCode();
        }
        return codes;
    }

    @Override
    public String toString() {
        return this.description + "(" + this.code + ")";
    }
}
