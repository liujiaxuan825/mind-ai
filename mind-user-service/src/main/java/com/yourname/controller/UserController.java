package com.yourname.controller;

import com.yourname.domain.dto.UserLoginDTO;
import com.yourname.domain.dto.UserRegisterDTO;
import com.yourname.domain.vo.UserLoginVO;
import com.yourname.domain.vo.UserVO;
import com.yourname.mind.common.Result;
import com.yourname.mind.exception.BusinessException;
import com.yourname.mind.service.TokenBlacklistService;
import com.yourname.service.MindUserService;
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