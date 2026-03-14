package com.github.cloudgyb.ai.knowledge.server.modules.rag;

import com.github.cloudgyb.ai.knowledge.server.config.PgVectorEmbeddingStorageProperties;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.DefaultMetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EmbeddingStore 工厂类
 *
 * @author cloudgyb
 * @since 2026/3/10 13:47
 */
@Component
public class EmbeddingStoreFactory {
    private final PgVectorEmbeddingStorageProperties properties;
    private final ConcurrentHashMap<Integer, EmbeddingStore<TextSegment>> embeddingStoreCacheMap;

    public EmbeddingStoreFactory(PgVectorEmbeddingStorageProperties properties) {
        this.properties = properties;
        this.embeddingStoreCacheMap = new ConcurrentHashMap<>();
    }

    public EmbeddingStore<TextSegment> create(int dimension) {
        return embeddingStoreCacheMap.computeIfAbsent(dimension, key -> doCreate(dimension));
    }

    private EmbeddingStore<TextSegment> doCreate(int dimension) {
        DefaultMetadataStorageConfig storageConfig = DefaultMetadataStorageConfig.builder()
                .storageMode(properties.getMetadataStorageMode())
                .columnDefinitions(Collections.singletonList("metadata JSON NULL"))
                .build();
        return PgVectorEmbeddingStore.builder()
                .host(properties.getHost())
                .port(properties.getPort())
                .database(properties.getDatabase())
                .user(properties.getUser())
                .password(properties.getPassword())
                .dimension(dimension)
                .table(properties.getTable() + "_" + dimension)
                .useIndex(true)
                .indexListSize(1000)
                .dropTableFirst(false)
                .metadataStorageConfig(storageConfig)
                .build();
    }

    @PreDestroy
    public void destroy() {
        embeddingStoreCacheMap.values().forEach(store -> {
            if (store instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) store).close();
                } catch (Exception e) {
                    // 记录日志
                }
            }
        });
    }
}
