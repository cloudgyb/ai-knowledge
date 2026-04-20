package com.github.cloudgyb.ai.knowledge.server.modules.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatConversation;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.service.AiChatConversationService;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.service.AiChatService;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.vo.ChatConversationMsgVO;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.ApiResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @author cloudgyb
 * @since 2026/3/17 21:11
 */
@RestController
@RequestMapping("/ai/chat")
public class AiChatController {
    private final AiChatService aiChatService;
    private final AiChatConversationService aiChatConversationService;

    public AiChatController(AiChatService aiChatService,
                            AiChatConversationService aiChatConversationService) {
        this.aiChatService = aiChatService;
        this.aiChatConversationService = aiChatConversationService;
    }

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@NotNull @RequestParam("cid") Long cid,
                              @NotNull @RequestParam("text") String text,
                              @RequestParam(value = "kbId", required = false) Integer kbId) {
        return aiChatService.chat(cid, text, kbId);
    }

    @GetMapping("/c")
    public ApiResponse<Page<ChatConversation>> getConversations(@RequestParam("pageNum") int pageNum,
                                                                @RequestParam("pageSize") int pageSize) {
        Page<ChatConversation> page = aiChatConversationService.page(pageNum, pageSize);
        return ApiResponse.success(page);
    }

    @GetMapping("/c/history")
    public ApiResponse<List<ChatConversationMsgVO>> getConversationMsgs(@RequestParam("id") Long id) {
        List<ChatConversationMsgVO> conversationMsgs = aiChatConversationService.getConversationMsgs(id);
        return ApiResponse.success(conversationMsgs);
    }

    @PostMapping("/c")
    public ApiResponse<String> addConversation(@RequestParam(value = "title", required = false) String title) {
        return ApiResponse.success(aiChatConversationService.addConversation(title));
    }

    @PutMapping("/c")
    public ApiResponse<Void> updateConversation(@RequestParam("id") Long id,
                                                @RequestParam(value = "title") String title) {
        aiChatConversationService.updateConversationTitle(id, title);
        return ApiResponse.success();
    }

    @DeleteMapping("/c")
    public ApiResponse<Void> deleteConversation(@RequestParam("id") Long id) {
        aiChatConversationService.deleteConversation(id);
        return ApiResponse.success();
    }

}
