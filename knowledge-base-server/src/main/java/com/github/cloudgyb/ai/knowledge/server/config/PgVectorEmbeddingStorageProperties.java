package com.github.cloudgyb.ai.knowledge.server.config;

import dev.langchain4j.store.embedding.pgvector.MetadataStorageMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * PGVector 向量存储配置
 *
 * @author cloudgyb
 * @since 2026/3/3 15:32
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "ai.embedding.storage.pgvector")
@Configuration
public class PgVectorEmbeddingStorageProperties {
    private String host;
    private Integer port;
    private String database;
    private String user;
    private String password;
    private String table;
    private MetadataStorageMode metadataStorageMode;

}
