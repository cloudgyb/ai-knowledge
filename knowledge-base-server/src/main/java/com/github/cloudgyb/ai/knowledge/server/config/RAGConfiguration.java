package com.github.cloudgyb.ai.knowledge.server.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.DefaultMetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * AI RAG 相关 bean 配置
 *
 * @author cloudgyb
 * 2026/2/26 14:04
 */
@Configuration
@EnableConfigurationProperties(PgVectorEmbeddingStorageProperties.class)
public class RAGConfiguration {

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

   // @Bean
    public EmbeddingStore<TextSegment> embeddingStore(PgVectorEmbeddingStorageProperties properties) {
        DefaultMetadataStorageConfig storageConfig = DefaultMetadataStorageConfig.builder()
                .storageMode(properties.getMetadataStorageMode())
                .columnDefinitions(Collections.singletonList("metadata JSON NULL"))
                .build();
        int dimension = embeddingModel().dimension();
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

}
