package com.liu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.common.common.Result;
import com.liu.user.domain.dto.RoleDTO;
import com.liu.user.domain.entity.Role;
import com.liu.user.domain.vo.RoleVO;

import java.util.List;

public interface MindRoleService extends IService<Role> {

    Result<Void> add(RoleDTO roleDTO);

    Result<Void> updateRole(RoleDTO roleDTO);

    Result<Void> deleteRole(Long id);

    Result<List<RoleVO>> listAll();
}
