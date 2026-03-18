package com.github.cloudgyb.ai.knowledge.server.modules.ai.service;

import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBase;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.service.KnowledgeBaseDocService;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.service.KnowledgeBaseService;
import com.github.cloudgyb.ai.knowledge.server.modules.rag.EmbeddingModelFactory;
import com.github.cloudgyb.ai.knowledge.server.modules.rag.EmbeddingStoreFactory;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

/**
 * @author cloudgyb
 * @since 2026/3/17 21:26
 */
@Slf4j
@Service
public class AiChatService {
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final AiModelService aiModelService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final KnowledgeBaseDocService knowledgeBaseDocService;
    private final EmbeddingModelFactory embeddingModelFactory;
    private final EmbeddingStoreFactory embeddingStoreFactory;

    public AiChatService(ThreadPoolTaskExecutor threadPoolTaskExecutor,
                         AiModelService aiModelService,
                         KnowledgeBaseService knowledgeBaseService,
                         KnowledgeBaseDocService knowledgeBaseDocService,
                         EmbeddingModelFactory embeddingModelFactory,
                         EmbeddingStoreFactory embeddingStoreFactory) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.aiModelService = aiModelService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.knowledgeBaseDocService = knowledgeBaseDocService;
        this.embeddingModelFactory = embeddingModelFactory;
        this.embeddingStoreFactory = embeddingStoreFactory;
    }

    public SseEmitter chat(String t, String text, Integer kbId) {
        SseEmitter sseEmitter = new SseEmitter(60000L);
        sseEmitter.onCompletion(() -> System.out.println("连接完成"));
        sseEmitter.onTimeout(() -> System.out.println("连接超时"));
        sseEmitter.onError(throwable -> System.out.println("连接错误"));
        threadPoolTaskExecutor.execute(() -> {
            try {
                doChat(sseEmitter, t, text, kbId);
            } catch (Exception e) {
                sseEmitter.complete();
            }
        });
        return sseEmitter;
    }

    private void doChat(SseEmitter sseEmitter, String t, String text, Integer kbId) throws IOException {
        KnowledgeBase knowledgeBase = knowledgeBaseService.getById(kbId == null ? 0 : kbId);
        if (knowledgeBase == null) {
            sseEmitter.send(SseEmitter.event().data("啊哦！当前选择的知识库不存在，可能被删除了！").build());
            sseEmitter.complete();
            return;
        }
        Integer aiVectorModelId = knowledgeBase.getAiVectorModelId();
        AiModel aiModel = aiModelService.getById(aiVectorModelId);
        if (aiModel == null) {
            sseEmitter.send(SseEmitter.event().data("啊哦！当前知识库使用的的向量模型不存在！").build());
            sseEmitter.complete();
            return;
        }
        List<Integer> docIds = knowledgeBaseDocService.listIdsByKbId(knowledgeBase.getId());
        EmbeddingModel embeddingModel = embeddingModelFactory.create(aiModel);
        EmbeddingStore<TextSegment> embeddingStore =
                embeddingStoreFactory.create(embeddingModel.dimension());
        QwenStreamingChatModel streamingChatModel = QwenStreamingChatModel.builder()
                .modelName("qwen-max")
                .apiKey("sk-14503a670fd54e7e846bc067b77cb025")
                .isMultimodalModel(false)
                .build();
        EmbeddingStoreContentRetriever embeddingStoreContentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(5)
                .minScore(0.65)
                .filter(MetadataFilterBuilder.metadataKey("docId").isIn(docIds))
                .build();
        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(streamingChatModel)
                .contentRetriever(embeddingStoreContentRetriever)
                .build();
        TokenStream stream = assistant.chat(text);
        stream.onPartialResponse(content -> {
            log.info("partial response: {}", content);
            try {
                sseEmitter.send(SseEmitter.event().data(content).build());
            } catch (IOException e) {
                sseEmitter.complete();
                throw new RuntimeException(e);
            }

        });
        stream.onError(throwable -> {
            try {
                log.error("error: ", throwable);
                sseEmitter.send(SseEmitter.event().data("啊哦！聊天发生错误！").build());
            } catch (IOException e) {
                sseEmitter.complete();
                throw new RuntimeException(e);
            }
        });
        stream.onCompleteResponse(chatResponse -> {
            sseEmitter.complete();
            TokenUsage tokenUsage = chatResponse.tokenUsage();
            log.info("tokenUsage: {}", tokenUsage);
        });
        stream.start();
    }

    interface Assistant {
        TokenStream chat(String userMessage);
    }

}
