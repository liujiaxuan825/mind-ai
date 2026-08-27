package com.liu.user.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignPermissionDTO {

    private Long roleId;

    /**
     * 覆盖写入：该角色最终只拥有这份权限列表
     */
    private List<Long> permissionIds;
}
