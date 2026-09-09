package com.liu.user.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.liu.user.common.RbacConstant;
import com.liu.user.domain.dto.UserLoginDTO;
import com.liu.user.domain.dto.UserRegisterDTO;
import com.liu.user.domain.vo.UserLoginVO;
import com.liu.user.domain.vo.UserVO;
import com.liu.common.common.Result;
import com.liu.user.service.MindUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@RefreshScope
public class UserController {
    private final MindUserService mindUserService;


    @PostMapping("/login")
    public Result<UserLoginVO> userLogin(@RequestBody @Valid UserLoginDTO userLoginDTO) {
        return mindUserService.login(userLoginDTO);
    }

    @PostMapping("/register")
    public Result<Void> userRegister(@RequestBody @Valid UserRegisterDTO userRegisterDTO) {
        return mindUserService.register(userRegisterDTO);
    }

    @PostMapping("/logout")
    public Result<Void> userLogout(){
        return mindUserService.logout();
    }

    @GetMapping("/me")
    public Result<UserVO> getMe() {
        return mindUserService.getMe();
    }

    @SaCheckRole(RbacConstant.ADMIN_ROLE_CODE)
    @GetMapping("/list")
    public Result<List<UserVO>> listUsers() {
        return mindUserService.listUsers();
    }

}
