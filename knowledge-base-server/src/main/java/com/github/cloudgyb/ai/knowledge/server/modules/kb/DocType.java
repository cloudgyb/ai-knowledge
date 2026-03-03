package com.github.cloudgyb.ai.knowledge.server.modules.kb;

/**
 *
 * @author cloudgyb
 * @since 2026/3/3 16:59
 */
public enum DocType {
    FILE("文件"),
    TEXT("文本");
    private final String desc;

    DocType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
