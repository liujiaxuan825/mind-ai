package com.liu.common.config.globalexceptionConfig;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.liu.common.common.Result;
import com.liu.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;


@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<?>> handleNoHandlerFound(NoHandlerFoundException e) {
        log.warn("请求路径不存在: {}", e.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error("请求路径不存在"));
    }
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error("内部出错了，请联系官人哨子~");
    }

    @ExceptionHandler(FlowException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<Void> handleFlowException(FlowException e) {
        log.warn("限流异常: {}", e.getMessage());
        return Result.flowLimitError();
    }

    @ExceptionHandler(DegradeException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result<Void> handleDegradeException(DegradeException e) {
        log.warn("[Sentinel 熔断] 触发熔断规则，规则详情：{}", e.getRule());
        return Result.degradeError();
    }
}