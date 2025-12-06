package com.yourname.service;

import com.yourname.domain.dto.UserLoginDTO;
import com.yourname.domain.dto.UserRegisterDTO;
import com.yourname.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yourname.domain.vo.UserLoginVO;
import com.yourname.domain.vo.UserVO;
import com.yourname.mind.common.Result;


/**
* @author liujiaxuan
* @description 针对表【mind_user】的数据库操作Service
* @createDate 2025-11-17 12:51:41
*/
public interface MindUserService extends IService<User> {

    Result<UserLoginVO> login(UserLoginDTO userLoginDTO);

    Result<Void> register(UserRegisterDTO userRegisterDTO);

    Result<UserVO> getMe();


    Result<Void> logout();
}
