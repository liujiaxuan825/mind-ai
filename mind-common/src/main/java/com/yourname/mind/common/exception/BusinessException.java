package com.yourname.mind.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
    private String msg;
    private Integer code;

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }
    public BusinessException(String msg) {
        super(msg);
        this.msg = msg;
    }
}
