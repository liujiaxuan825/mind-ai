package com.yourname.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterDTO {

    @NotBlank(message = "用户名不能为空！")
    private String username;

    @NotBlank(message = "密码不能为空！")
    private String password;

    @NotBlank(message = "密码不能为空！")
    private String passwordAgain;

    private String email;

    @NotBlank(message = "昵称不能为空！")
    private String nickname;

    @NotBlank(message = "电话不能为空！")
    private String phone;
}
