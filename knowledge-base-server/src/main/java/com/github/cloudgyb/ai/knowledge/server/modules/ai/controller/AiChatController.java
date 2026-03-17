package com.github.cloudgyb.ai.knowledge.server.modules.ai.controller;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author cloudgyb
 * @since 2026/3/17 21:11
 */
@RestController
@RequestMapping("/ai/chat")
public class AiChatController {

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@NotNull @RequestParam("t") String t,
                              @NotNull @RequestParam("text") String text,
                              @NotNull @RequestParam("kbId") Integer kbId) {
        SseEmitter sseEmitter = new SseEmitter();
        sseEmitter.onCompletion(() -> System.out.println("连接完成"));
        sseEmitter.onTimeout(() -> System.out.println("连接超时"));
        sseEmitter.onError(throwable -> System.out.println("连接错误"));
        sseEmitter.complete();
        return sseEmitter;
    }


}
