package com.github.cloudgyb.ai.knowledge.server.modules.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 聊天消息元数据
 */
@Data
public class ChatMessageMetadata {

    private TokenUsage tokenUsage;

    public void setTokenUsage(dev.langchain4j.model.output.TokenUsage tokenUsage) {
        this.tokenUsage = new TokenUsage(tokenUsage.inputTokenCount(), tokenUsage.outputTokenCount(),
                tokenUsage.totalTokenCount());
    }

    public record TokenUsage(Integer inputTokenCount, Integer outputTokenCount, Integer totalTokenCount) {
    }
}
