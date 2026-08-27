package com.liu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liu.common.common.Result;
import com.liu.user.domain.dto.PermissionDTO;
import com.liu.user.domain.entity.Permission;
import com.liu.user.domain.vo.PermissionVO;

import java.util.List;

public interface MindPermissionService extends IService<Permission> {

    Result<Void> add(PermissionDTO permissionDTO);

    Result<Void> updatePermission(PermissionDTO permissionDTO);

    Result<Void> deletePermission(Long id);

    Result<List<PermissionVO>> listAll();
}
