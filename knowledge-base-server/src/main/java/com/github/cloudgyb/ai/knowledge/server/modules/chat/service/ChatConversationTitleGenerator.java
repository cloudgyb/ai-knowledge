package com.github.cloudgyb.ai.knowledge.server.modules.chat.service;

import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Service;

/**
 * 聊天会话标题生成器
 *
 * @author cloudgyb
 * @since 2026-04-22 10:24:12
 */
@Service
public class ChatConversationTitleGenerator {

    public String generateTitle(ChatModel chatModel, String question) {
        return "标题";
    }
}
