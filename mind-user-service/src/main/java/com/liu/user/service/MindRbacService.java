package com.liu.user.service;

import com.liu.common.common.Result;
import com.liu.user.domain.dto.AssignPermissionDTO;
import com.liu.user.domain.dto.AssignRoleDTO;
import com.liu.user.domain.vo.PermissionVO;
import com.liu.user.domain.vo.RoleVO;

import java.util.List;

public interface MindRbacService {

    Result<Void> assignRoles(AssignRoleDTO dto);

    Result<Void> assignPermissions(AssignPermissionDTO dto);

    Result<List<RoleVO>> listRolesByUserId(Long userId);

    Result<List<PermissionVO>> listPermissionsByRoleId(Long roleId);

    Result<List<PermissionVO>> listMyPermissions();
}
