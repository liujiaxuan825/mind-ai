package com.liu.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liu.common.common.Result;
import com.liu.common.common.constant.RedisConstant;
import com.liu.common.exception.BusinessException;
import com.liu.user.domain.dto.AssignPermissionDTO;
import com.liu.user.domain.dto.AssignRoleDTO;
import com.liu.user.domain.entity.Permission;
import com.liu.user.domain.entity.Role;
import com.liu.user.domain.entity.RolePermission;
import com.liu.user.domain.entity.User;
import com.liu.user.domain.entity.UserRole;
import com.liu.user.domain.vo.PermissionVO;
import com.liu.user.domain.vo.RoleVO;
import com.liu.user.domain.vo.UserInfo;
import com.liu.user.mapper.MindPermissionMapper;
import com.liu.user.mapper.MindRoleMapper;
import com.liu.user.mapper.MindRolePermissionMapper;
import com.liu.user.mapper.MindUserMapper;
import com.liu.user.mapper.MindUserRoleMapper;
import com.liu.user.service.MindRbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MindRbacServiceImpl implements MindRbacService {

    private final MindUserMapper mindUserMapper;
    private final MindRoleMapper mindRoleMapper;
    private final MindPermissionMapper mindPermissionMapper;
    private final MindUserRoleMapper mindUserRoleMapper;
    private final MindRolePermissionMapper mindRolePermissionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> assignRoles(AssignRoleDTO dto) {
        if (dto == null || dto.getUserId() == null) {
            throw new BusinessException("用户 id 不能为空");
        }
        User user = mindUserMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        List<Long> roleIds = dto.getRoleIds() == null ? List.of() : dto.getRoleIds().stream().distinct().toList();
        if (!roleIds.isEmpty()) {
            Long count = mindRoleMapper.selectCount(new LambdaQueryWrapper<Role>().in(Role::getId, roleIds));
            if (count == null || count != roleIds.size()) {
                throw new BusinessException("存在无效角色 id");
            }
        }
        mindUserRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, dto.getUserId()));
        if (!CollectionUtils.isEmpty(roleIds)) {
            LocalDateTime now = LocalDateTime.now();
            List<UserRole> userRoles = roleIds.stream()
            .map(roleId -> new UserRole(dto.getUserId(), roleId, now)).toList();
            mindUserRoleMapper.insert(userRoles);
        }
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> assignPermissions(AssignPermissionDTO dto) {
        if (dto == null || dto.getRoleId() == null) {
            throw new BusinessException("角色 id 不能为空");
        }
        Role role = mindRoleMapper.selectById(dto.getRoleId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        List<Long> permissionIds = dto.getPermissionIds() == null ? List.of() : dto.getPermissionIds().stream().distinct().toList();
        if (!permissionIds.isEmpty()) {
            Long count = mindPermissionMapper.selectCount(new LambdaQueryWrapper<Permission>().in(Permission::getId, permissionIds));
            if (count == null || count != permissionIds.size()) {
                throw new BusinessException("存在无效权限 id");
            }
        }
        mindRolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, dto.getRoleId()));
        if (!CollectionUtils.isEmpty(permissionIds)) {
            LocalDateTime now = LocalDateTime.now();
            List<RolePermission> rolePermissions = permissionIds.stream().map(permissionId -> new RolePermission(dto.getRoleId(), permissionId, now)).toList();
            mindRolePermissionMapper.insert(rolePermissions);
        }
        return Result.success();
    }

    @Override
    public Result<List<RoleVO>> listRolesByUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户 id 不能为空");
        }
        if (mindUserMapper.selectById(userId) == null) {
            throw new BusinessException("用户不存在");
        }
        List<Role> roles = mindRoleMapper.selectRolesByUserId(userId);
        return Result.success(toRoleVOList(roles));
    }

    @Override
    public Result<List<PermissionVO>> listPermissionsByRoleId(Long roleId) {
        if (roleId == null) {
            throw new BusinessException("角色 id 不能为空");
        }
        if (mindRoleMapper.selectById(roleId) == null) {
            throw new BusinessException("角色不存在");
        }
        List<Permission> permissions = mindPermissionMapper.selectByRoleId(roleId);
        return Result.success(toPermissionVOList(permissions));
    }

    @Override
    public Result<List<PermissionVO>> listMyPermissions() {
        UserInfo userInfo = (UserInfo) StpUtil.getSession().get(RedisConstant.userInfoKey);
        if (userInfo == null || userInfo.getId() == null) {
            throw new BusinessException("请先登录");
        }
        List<Permission> permissions = mindPermissionMapper.selectEnabledByUserId(userInfo.getId());
        return Result.success(toPermissionVOList(permissions));
    }

    private List<RoleVO> toRoleVOList(List<Role> roles) {
        if (roles == null) {
            return new ArrayList<>();
        }
        return roles.stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    private List<PermissionVO> toPermissionVOList(List<Permission> permissions) {
        if (permissions == null) {
            return new ArrayList<>();
        }
        return permissions.stream().map(permission -> {
            PermissionVO vo = new PermissionVO();
            BeanUtils.copyProperties(permission, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
