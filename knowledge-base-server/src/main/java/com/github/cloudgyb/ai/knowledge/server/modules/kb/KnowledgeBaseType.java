package com.github.cloudgyb.ai.knowledge.server.modules.kb;

/**
 * 知识库类型枚举
 *
 * @author cloudgyb
 * @since 2026/3/2 21:59
 */
public enum KnowledgeBaseType {
    KNOWLEDGE_BASE("知识库"),
    MEMORY_BASE("记忆库");
    private final String type;

    KnowledgeBaseType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
