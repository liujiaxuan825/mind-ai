package com.liu.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.common.common.Result;
import com.liu.common.exception.BusinessException;
import com.liu.user.domain.dto.RoleDTO;
import com.liu.user.domain.entity.Role;
import com.liu.user.domain.entity.RolePermission;
import com.liu.user.domain.entity.UserRole;
import com.liu.user.domain.vo.RoleVO;
import com.liu.user.mapper.MindRoleMapper;
import com.liu.user.mapper.MindRolePermissionMapper;
import com.liu.user.mapper.MindUserRoleMapper;
import com.liu.user.service.MindRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MindRoleServiceImpl extends ServiceImpl<MindRoleMapper, Role> implements MindRoleService {

    private final MindUserRoleMapper mindUserRoleMapper;
    private final MindRolePermissionMapper mindRolePermissionMapper;

    @Override
    public Result<Void> add(RoleDTO roleDTO) {
        if (roleDTO == null || !StringUtils.hasText(roleDTO.getRoleCode()) || !StringUtils.hasText(roleDTO.getRoleName())) {
            throw new BusinessException("角色编码和名称不能为空");
        }
        Role exist = this.lambdaQuery().eq(Role::getRoleCode, roleDTO.getRoleCode()).one();
        if (exist != null) {
            throw new BusinessException("角色编码已存在");
        }
        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        role.setId(null);
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        LocalDateTime now = LocalDateTime.now();
        role.setCreateTime(now);
        role.setUpdateTime(now);
        save(role);
        return Result.success();
    }

    @Override
    public Result<Void> updateRole(RoleDTO roleDTO) {
        if (roleDTO == null || roleDTO.getId() == null) {
            throw new BusinessException("角色 id 不能为空");
        }
        Role role = getById(roleDTO.getId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        if (StringUtils.hasText(roleDTO.getRoleCode()) && !roleDTO.getRoleCode().equals(role.getRoleCode())) {
            Role exist = this.lambdaQuery().eq(Role::getRoleCode, roleDTO.getRoleCode()).one();
            if (exist != null) {
                throw new BusinessException("角色编码已存在");
            }
            role.setRoleCode(roleDTO.getRoleCode());
        }
        if (StringUtils.hasText(roleDTO.getRoleName())) {
            role.setRoleName(roleDTO.getRoleName());
        }
        if (roleDTO.getDescription() != null) {
            role.setDescription(roleDTO.getDescription());
        }
        if (roleDTO.getStatus() != null) {
            role.setStatus(roleDTO.getStatus());
        }
        role.setUpdateTime(LocalDateTime.now());
        updateById(role);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteRole(Long id) {
        if (id == null) {
            throw new BusinessException("角色 id 不能为空");
        }
        if (getById(id) == null) {
            throw new BusinessException("角色不存在");
        }
        mindUserRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        mindRolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));
        removeById(id);
        return Result.success();
    }

    @Override
    public Result<List<RoleVO>> listAll() {
        List<RoleVO> list = list().stream().map(role -> {
            RoleVO vo = new RoleVO();
            BeanUtils.copyProperties(role, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.success(list);
    }
}
