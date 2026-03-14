package com.github.cloudgyb.ai.knowledge.server.modules.commons;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @author cloudgyb
 * @since 2026/3/14 19:19
 */
@Getter
public class BusinessException extends RuntimeException {
    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = "500";
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = "500";
    }


    public BusinessException(String message, Throwable cause, String code) {
        super(message, cause);
        this.code = code;
    }

    public BusinessException(Throwable cause, String code) {
        super(cause);
        this.code = code;
    }

}
