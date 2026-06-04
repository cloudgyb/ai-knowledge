package com.github.cloudgyb.ai.knowledge.server.modules.chat;

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;

/**
 * 包装 SseEmitter,自动处理发送过程中的异常,如果流关闭则忽略发送
 *
 * @author cloudgyb
 * @since 2026/6/4 16:11
 */
public class ChatSseEmitter extends SseEmitter {
    private final Logger log = org.slf4j.LoggerFactory.getLogger(getClass());
    private volatile boolean isClosed;

    public ChatSseEmitter() {
        super();
        this.isClosed = false;
    }


    public ChatSseEmitter(Long timeout) {
        super(timeout);
        this.isClosed = false;
    }

    @Override
    public void send(@NonNull SseEventBuilder builder) {
        try {
            if (!isClosed) {
                super.send(builder);
            }
        } catch (IllegalStateException | IOException e) {
            this.isClosed = true;
            log.error("ChatSseEmitter exception:", e);
        }
    }

    @Override
    public void send(@NonNull Object object, @Nullable MediaType mediaType) {
        try {
            if (!isClosed) {
                super.send(object, mediaType);
            }
        } catch (IllegalStateException | IOException e) {
            this.isClosed = true;
            log.error("ChatSseEmitter exception:", e);
        }
    }

    @Override
    public void send(@NonNull Object object) {
        try {
            if (!isClosed) {
                super.send(object);
            }
        } catch (IllegalStateException |  IOException e) {
            this.isClosed = true;
            log.error("ChatSseEmitter exception:", e);
        }
    }

    @Override
    public void send(@NonNull Set<DataWithMediaType> items) {
        try {
            if (!isClosed) {
                super.send(items);
            }
        } catch (IllegalStateException | IOException e) {
            this.isClosed = true;
            log.error("ChatSseEmitter exception:", e);
        }
    }
}
