package com.liu.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.common.common.constant.RedisConstant;
import com.liu.common.common.domain.UserInfo;
import com.liu.user.domain.dto.UserLoginDTO;
import com.liu.user.domain.dto.UserRegisterDTO;
import com.liu.user.domain.entity.Role;
import com.liu.user.domain.entity.User;
import com.liu.user.domain.vo.MenuVO;
import com.liu.user.domain.vo.UserLoginVO;
import com.liu.user.domain.vo.UserVO;
import com.liu.user.enumsPack.UserStatus;
import com.liu.common.common.Result;
import com.liu.common.exception.BusinessException;
import com.liu.user.common.RbacConstant;
import com.liu.user.service.MindMenuService;
import com.liu.user.service.MindUserService;
import com.liu.user.mapper.MindRoleMapper;
import com.liu.user.mapper.MindUserMapper;
import com.liu.user.mapper.MindUserRoleMapper;
import com.liu.user.domain.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author liujiaxuan
* @description 针对表【mind_user】的数据库操作Service实现
* @createDate 2025-11-17 12:51:41
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class MindUserServiceImpl extends ServiceImpl<MindUserMapper, User> implements MindUserService {

    private final MindRoleMapper mindRoleMapper;
    private final MindMenuService mindMenuService;
    private final MindUserRoleMapper mindUserRoleMapper;

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
        //一键登录，生成token，redis中token->user_id的映射， userid->user_info的映射
        StpUtil.login(user.getId());

        LocalDateTime now = LocalDateTime.now();
        this.lambdaUpdate().eq(User::getUsername, name).set(User::getLastLoginTime, now).update();

        UserVO userInfo = toUserVO(user);
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setToken(StpUtil.getTokenValue());
        userLoginVO.setUserInfo(userInfo);
        userLoginVO.setLoginTime(now);

        // ============== Token 过期时间（BUG 9：补全 expiresIn / expiresAt）==============
        // StpUtil.getTokenTimeout() 返回剩余秒数：>0 表示剩余秒数；-2 表示永不过期；-1 表示无 token
        long timeout = StpUtil.getTokenTimeout();
        if (timeout > 0) {
            userLoginVO.setExpiresIn(timeout);
            userLoginVO.setExpiresAt(now.plusSeconds(timeout));
        }
        // 永不过期或无 token 场景下保持 null，前端按 "长期有效" 处理即可

        // ============== 动态菜单加载 ==============
        // 校验登录后按用户角色加载菜单：
        //  - 普通用户：AI对话 / 知识库 / 全局搜索 / 个人中心
        //  - 管理员：多出"系统管理"目录（角色管理 / 权限管理 / RBAC管理）
        List<MenuVO> menus = mindMenuService.listMenusByUserId(user.getId());
        userLoginVO.setMenus(menus);
        log.info("[登录-菜单加载] 用户 {}({}) 角色编码={}，返回菜单数={}",
                user.getUsername(), user.getId(), userInfo.getRoleCodes(),
                menus == null ? 0 : menus.size());

        StpUtil.getSession().set(RedisConstant.userInfoKey, new UserInfo(user.getId(), user.getUsername()));
        return Result.success(userLoginVO);
    }

    public Result<UserLoginVO> loginBlock(UserLoginDTO userLoginDTO, BlockException e) throws BlockException {
        log.warn("[Sentinel] 登录接口被拦截 → 规则：{}", e.getRule());
        throw e;
    }

    @Override
    @SentinelResource(value = "user:register", blockHandler = "registerBlock")
    @Transactional(rollbackFor = Exception.class)
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

        // ============== BUG 2 修复：注册后分配默认 USER 角色 ==============
        // 默认角色由初始化脚本保证存在；这里只负责"读取并分配"，不自动建角色。
        // 若默认角色缺失，抛明确异常提示管理员初始化，避免静默导致新用户菜单为空。
        Role defaultRole = mindRoleMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, RbacConstant.USER_ROLE_CODE));
        if (defaultRole == null) {
            throw new BusinessException("系统默认角色（USER）未初始化，请联系管理员");
        }
        mindUserRoleMapper.insert(new UserRole(mindUser.getId(), defaultRole.getId(), LocalDateTime.now()));
        log.info("[注册-默认角色分配] 新用户 {}({}) 已分配默认角色 {}",
                mindUser.getUsername(), mindUser.getId(), defaultRole.getRoleCode());

        return Result.success();
    }

    public Result<Void> registerBlock(UserRegisterDTO userRegisterDTO, BlockException e) throws BlockException {
        log.warn("[Sentinel] 注册接口被拦截 → 规则：{}", e.getRule());
        throw e;
    }

    @Override
    public Result<UserVO> getMe() {
        Object raw = StpUtil.getSession().get(RedisConstant.userInfoKey);
        if (!(raw instanceof UserInfo userInfo)) {
            // Session 里没有登录态（token 已失效或 session 被清理）
            throw new BusinessException("登录状态已失效，请重新登录");
        }
        User user = getById(userInfo.getId());
        if (user == null) {
            // 用户已被删除但 token 仍在有效期内：清掉登录态，避免拿到 null 的"成功"响应
            if (StpUtil.isLogin()) {
                StpUtil.logout();
            }
            throw new BusinessException("用户不存在或已被删除，请重新登录");
        }
        return Result.success(toUserVO(user));
    }

    @Override
    public Result<Void> logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        return Result.success();
    }

    @Override
    public Result<List<UserVO>> listUsers() {
        List<UserVO> list = list().stream().map(this::toUserVO).collect(Collectors.toList());
        return Result.success(list);
    }

    private UserVO toUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        List<String> roleCodes = mindRoleMapper.selectRolesByUserId(user.getId()).stream()
                .filter(role -> role.getStatus() != null && role.getStatus() == 1)
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
        userVO.setRoleCodes(roleCodes);
        return userVO;
    }

}
