package com.github.cloudgyb.ai.knowledge.server.modules.chat.domain;

import dev.langchain4j.model.output.TokenUsage;
import lombok.Data;

/**
 * 聊天消息元数据
 */
@Data
public class ChatMessageMetadata {

    private TokenUsage tokenUsage;
}
