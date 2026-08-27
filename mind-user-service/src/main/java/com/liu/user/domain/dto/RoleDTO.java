package com.liu.user.domain.dto;

import lombok.Data;

@Data
public class RoleDTO {

    private Long id;

    private String roleCode;

    private String roleName;

    private String description;

    /**
     * 1 启用 0 停用，不传默认 1
     */
    private Integer status;
}
