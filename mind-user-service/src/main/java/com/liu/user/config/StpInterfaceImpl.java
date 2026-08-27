package com.liu.user.config;

import cn.dev33.satoken.stp.StpInterface;
import com.liu.user.domain.entity.Permission;
import com.liu.user.domain.entity.Role;
import com.liu.user.mapper.MindPermissionMapper;
import com.liu.user.mapper.MindRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 为 Sa-Token 提供当前登录用户的角色编码、权限编码。
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final MindRoleMapper mindRoleMapper;
    private final MindPermissionMapper mindPermissionMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        return mindPermissionMapper.selectEnabledByUserId(userId).stream()
                .map(Permission::getPermCode)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        return mindRoleMapper.selectRolesByUserId(userId).stream()
                .filter(role -> role.getStatus() != null && role.getStatus() == 1)
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
    }
}
