package com.liu.ai.common;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int SUCCESS_CODE = 200;
    public static final String SUCCESS_MESSAGE = "success";
    public static final int COMMON_ERROR_CODE = 500;
    public static final int FLOW_LIMIT_ERROR_CODE = 500100;
    public static final String FLOW_LIMIT_ERROR_MESSAGE = "系统繁忙，当前请求量过大，请稍后再试";

    public static final int DEGRADE_ERROR_CODE = 500101;
    public static final String DEGRADE_ERROR_MESSAGE = "服务暂时不可用，请稍后重试";

    public static final int PARAM_FLOW_ERROR_CODE = 500102;
    public static final String PARAM_FLOW_ERROR_MESSAGE = "请求过于频繁，请稍后再试";

    public static final int SYSTEM_BLOCK_ERROR_CODE = 500103;
    public static final String SYSTEM_BLOCK_ERROR_MESSAGE = "系统负载过高，请稍后再试";

    public static final int AUTHORITY_ERROR_CODE = 500104;
    public static final String AUTHORITY_ERROR_MESSAGE = "无权限访问";

    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage(SUCCESS_MESSAGE);
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage(SUCCESS_MESSAGE);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(COMMON_ERROR_CODE);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> flowLimitError() {
        return error(FLOW_LIMIT_ERROR_CODE, FLOW_LIMIT_ERROR_MESSAGE);
    }

    public static <T> Result<T> flowLimitError(String message) {
        return error(FLOW_LIMIT_ERROR_CODE, message);
    }

    public static <T> Result<T> degradeError() {
        return error(DEGRADE_ERROR_CODE, DEGRADE_ERROR_MESSAGE);
    }

    public static <T> Result<T> degradeError(String message) {
        return error(DEGRADE_ERROR_CODE, message);
    }

    public static <T> Result<T> paramFlowError() {
        return error(PARAM_FLOW_ERROR_CODE, PARAM_FLOW_ERROR_MESSAGE);
    }

    public static <T> Result<T> paramFlowError(String message) {
        return error(PARAM_FLOW_ERROR_CODE, message);
    }

    public static <T> Result<T> systemBlockError() {
        return error(SYSTEM_BLOCK_ERROR_CODE, SYSTEM_BLOCK_ERROR_MESSAGE);
    }

    public static <T> Result<T> authorityError() {
        return error(AUTHORITY_ERROR_CODE, AUTHORITY_ERROR_MESSAGE);
    }
}
