package com.liu.user.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoleVO {

    private Long id;

    private String roleCode;

    private String roleName;

    private String description;

    private Integer status;

    private LocalDateTime createTime;
}
