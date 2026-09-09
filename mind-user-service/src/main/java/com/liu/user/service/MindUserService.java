package com.liu.user.service;

import com.liu.common.common.Result;
import com.liu.user.domain.dto.UserLoginDTO;
import com.liu.user.domain.dto.UserRegisterDTO;
import com.liu.user.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.user.domain.vo.UserLoginVO;
import com.liu.user.domain.vo.UserVO;

import java.util.List;



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

    Result<List<UserVO>> listUsers();
}
