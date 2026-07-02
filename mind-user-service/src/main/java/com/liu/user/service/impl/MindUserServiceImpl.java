package com.liu.user.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.common.untils.UserContext;
import com.liu.user.domain.dto.UserLoginDTO;
import com.liu.user.domain.dto.UserRegisterDTO;
import com.liu.user.domain.entity.User;
import com.liu.user.domain.vo.UserLoginVO;
import com.liu.user.domain.vo.UserVO;
import com.liu.user.enumsPack.UserStatus;

import com.liu.common.common.Result;
import com.liu.common.untils.JwtUtils;
import com.liu.common.exception.BusinessException;

import com.liu.common.service.TokenBlacklistService;
import com.liu.user.service.MindUserService;
import com.liu.user.mapper.MindUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
* @author liujiaxuan
* @description 针对表【mind_user】的数据库操作Service实现
* @createDate 2025-11-17 12:51:41
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class MindUserServiceImpl extends ServiceImpl<MindUserMapper, User> implements MindUserService {

    private final JwtUtils jwtUtils;

    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @SentinelResource(value = "user:login", blockHandler = "loginBlock")
    public Result<UserLoginVO> login(UserLoginDTO userLoginDTO) {
        String name = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();
        //判断是否为空
        if (name == null || password == null) {
            throw new BusinessException("用户名或者密码不能为空！");
        }
        //先查询用户是否存在
        User user = this.lambdaQuery().eq(User::getUsername, name).one();
        if (user == null) {
            throw new BusinessException("用户不存在！");
        }
        if (user.getStatus() == UserStatus.DISABLE) {
            throw new BusinessException("用户账号不可用！");
        }
        if (user.getStatus() == UserStatus.UNACTIVATED) {
            throw new BusinessException("用户状态未激活！");
        }
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
        if (!md5Password.equals(user.getPassword())) {
            throw new BusinessException("密码输入有误！");
        }
        LocalDateTime now = LocalDateTime.now();
        this.lambdaUpdate().eq(User::getUsername, name).set(User::getLastLoginTime, now).update();

        String token = jwtUtils.generateToken(user.getId(), name);
        UserVO userInfo = new UserVO();
        BeanUtils.copyProperties(user, userInfo);
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setToken(token);
        userLoginVO.setUserInfo(userInfo);
        return Result.success(userLoginVO);
    }

    public Result<UserLoginVO> loginBlock(UserLoginDTO userLoginDTO, BlockException e) throws BlockException {
        log.warn("[Sentinel] 登录接口被拦截 → 规则：{}", e.getRule());
        throw e;
    }

    @Override
    @SentinelResource(value = "user:register", blockHandler = "registerBlock")
    public Result<Void> register(UserRegisterDTO userRegisterDTO) {
        if (!userRegisterDTO.getPassword().equals(userRegisterDTO.getPasswordAgain())) {
            throw new BusinessException("两次输入密码不一致！");
        }
        User user = this.lambdaQuery().eq(User::getPhone, userRegisterDTO.getPhone()).one();
        if (user != null) {
            throw new BusinessException("用户已经存在！");
        }
        User username = this.lambdaQuery().eq(User::getUsername, userRegisterDTO.getUsername()).one();
        if (username != null) {
            throw new BusinessException("用户名已经存在啦，换一个试试呢~");
        }
        User mindUser = new User();
        BeanUtils.copyProperties(userRegisterDTO, mindUser);
        String MD5Password = DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes(StandardCharsets.UTF_8));
        mindUser.setPassword(MD5Password);
        save(mindUser);
        return Result.success();
    }

    public Result<Void> registerBlock(UserRegisterDTO userRegisterDTO, BlockException e) throws BlockException {
        log.warn("[Sentinel] 注册接口被拦截 → 规则：{}", e.getRule());
        throw e;
    }

    @Override
    public Result<UserVO> getMe() {
        Long userId = UserContext.getUserId();
        User user = getById(userId);
        if (user == null) {
            return null;
        }
        UserVO userInfo = new UserVO();
        BeanUtils.copyProperties(user, userInfo);
        return Result.success(userInfo);
    }

    @Override
    public Result<Void> logout() {
        String token = UserContext.getToken();
        if(token == null){
            return Result.error("令牌不存在！");
        }
        try {
            boolean b = jwtUtils.validateToken(token);
            if(!b){
                return Result.error("token解析失败！");
            }
            long remainingTimeInSeconds = jwtUtils.getRemainingTimeInSeconds(token);
            tokenBlacklistService.addCurrUserBlacklist(token,remainingTimeInSeconds);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(e.getMessage());
        }
        return Result.success();
    }

}