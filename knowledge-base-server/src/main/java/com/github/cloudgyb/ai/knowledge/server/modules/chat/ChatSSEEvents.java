package com.github.cloudgyb.ai.knowledge.server.modules.chat;

import lombok.Getter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;

/**
 * SSE 事件类型枚举
 *
 * @author cloudgyb
 * @since 2026/4/22 11:06
 */
@Getter
public enum ChatSSEEvents {
    THINKING("thinking"),
    CONTENT("content"),
    ERROR("error"),
    CLOSE("close");
    private final String eventName;

    ChatSSEEvents(String eventName) {
        this.eventName = eventName;
    }

    public static SseEmitter.SseEventBuilder error(String data) {
        return SseEmitter.event().name(ERROR.eventName).data(data);
    }

    public static SseEmitter.SseEventBuilder content(String data) {
        return SseEmitter.event().name(CONTENT.eventName).data(data);
    }

    public static SseEmitter.SseEventBuilder thinking(String data) {
        return SseEmitter.event().name(THINKING.eventName).data(data);
    }

    public static SseEmitter.SseEventBuilder close(String data) {
        return SseEmitter.event().name(CLOSE.eventName).data(data);
    }
}
