package com.github.cloudgyb.ai.knowledge.server.modules.kb;

/**
 *
 * @author cloudgyb
 * @since 2026/3/3 14:32
 */
public enum DocStatus {
    VECTORIZING("向量化中"),
    VECTORIZED("向量化完成"),
    FAILED("向量化失败");
    private final String desc;

    DocStatus(String desc) {
        this.desc = desc;
    }


    public String getDesc() {
        return desc;
    }

    public static DocStatus fromValue(int value) {
        for (DocStatus status : values()) {
            if (status.ordinal() == value) {
                return status;
            }
        }
        return null;
    }


}
