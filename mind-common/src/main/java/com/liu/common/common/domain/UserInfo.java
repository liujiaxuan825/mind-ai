package com.liu.common.common.domain;

import lombok.Data;

@Data
public class UserInfo {
    private Long id;
    private String username;

    public UserInfo() {
    }

    public UserInfo(Long id, String username) {
        this.id = id;
        this.username = username;
    }
}
