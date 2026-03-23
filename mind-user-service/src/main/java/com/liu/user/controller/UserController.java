package com.liu.user.controller;

import com.liu.user.domain.dto.UserLoginDTO;
import com.liu.user.domain.dto.UserRegisterDTO;
import com.liu.user.domain.vo.UserLoginVO;
import com.liu.user.domain.vo.UserVO;
import com.liu.common.mind.common.Result;
import com.liu.user.service.MindUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final MindUserService mindUserService;


    @PostMapping("/login")
    public Result<UserLoginVO> userLogin(@RequestBody UserLoginDTO userLoginDTO) {
        return mindUserService.login(userLoginDTO);
    }

    @PostMapping("/register")
    public Result<Void> userRegister(@RequestBody UserRegisterDTO userRegisterDTO) {
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

}