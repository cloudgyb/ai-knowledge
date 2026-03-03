package com.github.cloudgyb.ai.knowledge.server.config;

import dev.langchain4j.store.embedding.pgvector.MetadataStorageMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author cloudgyb
 * @since 2026/3/3 15:32
 */
@ConfigurationProperties(prefix = "ai.embedding.storage.pgvector")
public final class PgVectorEmbeddingStorageProperties {
    private String host;
    private Integer port;
    private String database;
    private String user;
    private String password;
    private String table;
    private MetadataStorageMode metadataStorageMode;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public MetadataStorageMode getMetadataStorageMode() {
        return metadataStorageMode;
    }

    public void setMetadataStorageMode(MetadataStorageMode metadataStorageMode) {
        this.metadataStorageMode = metadataStorageMode;
    }
}
