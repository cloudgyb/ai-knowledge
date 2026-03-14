package com.github.cloudgyb.ai.knowledge.server.modules.commons;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author cloudgyb
 * @since 2026/3/14 19:18
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler()
    @ResponseStatus
    public ApiResponse<Void> handleException(BusinessException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus
    public ApiResponse<Void> handleException(Throwable e) {
        return ApiResponse.error("500", e.getMessage());
    }
}
