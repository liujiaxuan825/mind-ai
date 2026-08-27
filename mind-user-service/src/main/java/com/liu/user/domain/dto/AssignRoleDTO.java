package com.liu.user.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignRoleDTO {

    private Long userId;

    /**
     * 覆盖写入：该用户最终只拥有这份角色列表
     */
    private List<Long> roleIds;
}
