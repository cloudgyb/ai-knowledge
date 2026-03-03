package com.github.cloudgyb.ai.knowledge.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 知识库文档存储配置属性
 *
 * @author cloudgyb
 * @since 2026/3/3 16:30
 */
@Configuration
@ConfigurationProperties(prefix = "knowledge-base.doc.storage")
public class KnowledgeBaseDocStorageProperties {

    /**
     * 文档存储路径
     */
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
