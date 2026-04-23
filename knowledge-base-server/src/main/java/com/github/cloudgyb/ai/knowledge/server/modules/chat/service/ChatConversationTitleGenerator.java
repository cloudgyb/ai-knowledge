package com.github.cloudgyb.ai.knowledge.server.modules.chat.service;

import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 聊天会话标题生成器
 *
 * @author cloudgyb
 * @since 2026-04-22 10:24:12
 */
@Service
public class ChatConversationTitleGenerator {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private volatile String generateTemplate;

    public String generateTitle(ChatModel chatModel, String question) {
        String prompt = generatePrompt(question, 20);
        try {
            return chatModel.chat(prompt);
        } catch (Exception e) {
            logger.error("生成标题失败{}", e.getMessage());
        }
        return question.length() <= 20 ? question : "新对话";
    }

    public void loadAiPromptTemplate() throws IOException {
        if (generateTemplate == null) {
            synchronized (this) {
                if (generateTemplate == null) {
                    ClassPathResource classPathResource = new ClassPathResource("prompt/conversation-title.md");
                    generateTemplate = classPathResource.getContentAsString(StandardCharsets.UTF_8);
                }
            }
        }
    }

    public String generatePrompt(String question, int titleMaxChars) {
        try {
            loadAiPromptTemplate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String replace = generateTemplate.replace("{title_max_chars}", titleMaxChars + "");
        return replace.replace("{question}", question);
    }
}
