package com.github.cloudgyb.ai.knowledge.server.modules.commons;

import cn.dev33.satoken.exception.NotLoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author cloudgyb
 * @since 2026/3/14 19:18
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler()
    @ResponseStatus
    public ApiResponse<Void> handleException(BusinessException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleException(NotLoginException e) {
        return ApiResponse.error(HttpStatus.UNAUTHORIZED.value() + "", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus
    @ResponseBody
    public ApiResponse<Void> handleException(Throwable e) {
        log.error("服务器错误", e);
        return ApiResponse.error("500", e.getMessage());
    }
}
