package com.yourname.mind.exception;

import lombok.Getter;


public class BusinessException extends RuntimeException {
    @Getter
    private final Integer code;
    private final String message;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}