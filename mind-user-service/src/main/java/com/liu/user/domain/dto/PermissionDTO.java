package com.liu.user.domain.dto;

import lombok.Data;

@Data
public class PermissionDTO {

    private Long id;

    private String permCode;

    private String permName;

    private String description;

    /**
     * 1 启用 0 停用，不传默认 1
     */
    private Integer status;
}
