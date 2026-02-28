package com.github.cloudgyb.ai.knowledge.server.modules.ai;

public enum AiModelType {
    LANG("语言模型"),
    VECTOR("向量模型"),
    IMAGE("图像模型");

    private final String desc;

    AiModelType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
