package com.liu.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liu.common.common.Result;
import com.liu.common.exception.BusinessException;
import com.liu.user.domain.dto.PermissionDTO;
import com.liu.user.domain.entity.Permission;
import com.liu.user.domain.entity.RolePermission;
import com.liu.user.domain.vo.PermissionVO;
import com.liu.user.mapper.MindPermissionMapper;
import com.liu.user.mapper.MindRolePermissionMapper;
import com.liu.user.service.MindPermissionService;
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
public class MindPermissionServiceImpl extends ServiceImpl<MindPermissionMapper, Permission> implements MindPermissionService {

    private final MindRolePermissionMapper mindRolePermissionMapper;

    @Override
    public Result<Void> add(PermissionDTO permissionDTO) {
        if (permissionDTO == null || !StringUtils.hasText(permissionDTO.getPermCode()) || !StringUtils.hasText(permissionDTO.getPermName())) {
            throw new BusinessException("权限编码和名称不能为空");
        }
        Permission exist = this.lambdaQuery().eq(Permission::getPermCode, permissionDTO.getPermCode()).one();
        if (exist != null) {
            throw new BusinessException("权限编码已存在");
        }
        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionDTO, permission);
        permission.setId(null);
        if (permission.getStatus() == null) {
            permission.setStatus(1);
        }
        LocalDateTime now = LocalDateTime.now();
        permission.setCreateTime(now);
        permission.setUpdateTime(now);
        save(permission);
        return Result.success();
    }

    @Override
    public Result<Void> updatePermission(PermissionDTO permissionDTO) {
        if (permissionDTO == null || permissionDTO.getId() == null) {
            throw new BusinessException("权限 id 不能为空");
        }
        Permission permission = getById(permissionDTO.getId());
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        if (StringUtils.hasText(permissionDTO.getPermCode()) && !permissionDTO.getPermCode().equals(permission.getPermCode())) {
            Permission exist = this.lambdaQuery().eq(Permission::getPermCode, permissionDTO.getPermCode()).one();
            if (exist != null) {
                throw new BusinessException("权限编码已存在");
            }
            permission.setPermCode(permissionDTO.getPermCode());
        }
        if (StringUtils.hasText(permissionDTO.getPermName())) {
            permission.setPermName(permissionDTO.getPermName());
        }
        if (permissionDTO.getDescription() != null) {
            permission.setDescription(permissionDTO.getDescription());
        }
        if (permissionDTO.getStatus() != null) {
            permission.setStatus(permissionDTO.getStatus());
        }
        permission.setUpdateTime(LocalDateTime.now());
        updateById(permission);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deletePermission(Long id) {
        if (id == null) {
            throw new BusinessException("权限 id 不能为空");
        }
        if (getById(id) == null) {
            throw new BusinessException("权限不存在");
        }
        mindRolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getPermissionId, id));
        removeById(id);
        return Result.success();
    }

    @Override
    public Result<List<PermissionVO>> listAll() {
        List<PermissionVO> list = list().stream().map(permission -> {
            PermissionVO vo = new PermissionVO();
            BeanUtils.copyProperties(permission, vo);
            return vo;
        }).collect(Collectors.toList());
        return Result.success(list);
    }
}
