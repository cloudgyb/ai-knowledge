package com.github.cloudgyb.ai.knowledge.server.modules.ai.controller;

import com.github.cloudgyb.ai.knowledge.server.modules.ai.service.AiChatService;
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
    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@NotNull @RequestParam("t") String t,
                              @NotNull @RequestParam("text") String text,
                              @RequestParam(value = "kbId", required = false) Integer kbId) {
        return aiChatService.chat(t, text, kbId);
    }


}
