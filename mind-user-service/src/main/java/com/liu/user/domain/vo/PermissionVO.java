package com.liu.user.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PermissionVO {

    private Long id;

    private String permCode;

    private String permName;

    private String description;

    private Integer status;

    private LocalDateTime createTime;
}
