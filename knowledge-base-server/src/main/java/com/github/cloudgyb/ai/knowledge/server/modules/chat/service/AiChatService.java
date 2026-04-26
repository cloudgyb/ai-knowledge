package com.github.cloudgyb.ai.knowledge.server.modules.chat.service;

import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.service.AiChatModelFactory;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.service.AiModelService;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.ChatSSEEvents;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.ConversationStatus;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.*;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.BusinessException;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBase;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.service.KnowledgeBaseDocService;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.service.KnowledgeBaseService;
import com.github.cloudgyb.ai.knowledge.server.modules.rag.EmbeddingModelFactory;
import com.github.cloudgyb.ai.knowledge.server.modules.rag.EmbeddingStoreFactory;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * AI 聊天服务实现，SSE 流式推送
 *
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
    private final ChatConversationService chatConversationService;
    private final ChatMessageService chatMessageService;
    private final AiChatModelFactory aiChatModelFactory;
    private final ChatConversationTitleGenerator chatConversationTitleGenerator;
    private final PersistentChatMemoryStore persistentChatMemoryStore;

    public AiChatService(ThreadPoolTaskExecutor threadPoolTaskExecutor,
                         AiModelService aiModelService,
                         KnowledgeBaseService knowledgeBaseService,
                         KnowledgeBaseDocService knowledgeBaseDocService,
                         EmbeddingModelFactory embeddingModelFactory,
                         EmbeddingStoreFactory embeddingStoreFactory,
                         ChatConversationService chatConversationService,
                         ChatMessageService chatMessageService,
                         AiChatModelFactory aiChatModelFactory,
                         ChatConversationTitleGenerator chatConversationTitleGenerator,
                         PersistentChatMemoryStore persistentChatMemoryStore) {
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.aiModelService = aiModelService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.knowledgeBaseDocService = knowledgeBaseDocService;
        this.embeddingModelFactory = embeddingModelFactory;
        this.embeddingStoreFactory = embeddingStoreFactory;
        this.chatConversationService = chatConversationService;
        this.chatMessageService = chatMessageService;
        this.aiChatModelFactory = aiChatModelFactory;
        this.chatConversationTitleGenerator = chatConversationTitleGenerator;
        this.persistentChatMemoryStore = persistentChatMemoryStore;
    }

    public SseEmitter chat(Long cid, String text, Integer kbId, Integer modelId) {
        ChatConversation chatConversation = chatConversationService.getById(cid);
        if (chatConversation == null) {
            throw new BusinessException("对话不存在！");
        }
        if (chatConversation.getCurrentStatus() == ConversationStatus.ACTIVE) {
            throw new BusinessException("对话正在进行中！");
        }

        SseEmitter sseEmitter = new SseEmitter(0L);
        sseEmitter.onCompletion(() -> System.out.println("连接完成"));
        sseEmitter.onTimeout(() -> System.out.println("连接超时"));
        sseEmitter.onError(throwable -> System.out.println("连接错误"));
        // 更新对话状态
        Date lastActiveTime = new Date();
        chatConversationService.updateConversationStatus(cid, ConversationStatus.ACTIVE, null, lastActiveTime);
        try {
            sseEmitter.send(ChatSSEEvents.lastActiveTime(DateFormatUtils.format(lastActiveTime, "yyyy-MM-dd HH:mm:ss")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 异步为新对话生成标题
        if (chatConversation.getCurrentStatus().equals(ConversationStatus.NEW)) {
            threadPoolTaskExecutor.execute(() -> {
                AiModel aiModel = aiModelService.getById(modelId);
                ChatModel chatModel = aiChatModelFactory.createChatModel(aiModel);
                String title = chatConversationTitleGenerator.generateTitle(chatModel, text);
                chatConversationService.updateConversationStatus(cid, null, title, null);
                try {
                    sseEmitter.send(ChatSSEEvents.title(title));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        // 异步生成回复
        threadPoolTaskExecutor.execute(() -> {
            try {
                doChat(sseEmitter, cid, text, kbId, modelId);
            } catch (Exception e) {
                log.error("对话错误", e);
                sseEmitter.complete();
                chatConversationService.updateConversationStatus(cid, ConversationStatus.FAILED, null, new Date());
            }
            chatConversationService.updateConversationStatus(cid, ConversationStatus.ENDED, null, new Date());
        });
        return sseEmitter;
    }

    private void doChat(SseEmitter sseEmitter, Long cid, String text, Integer kbId, Integer modelId) throws IOException {
        KnowledgeBase knowledgeBase;
        EmbeddingModel embeddingModel;
        EmbeddingStore<TextSegment> embeddingStore;
        EmbeddingStoreContentRetriever embeddingStoreContentRetriever = null;
        if (kbId != null) {
            knowledgeBase = knowledgeBaseService.getById(kbId);
            if (knowledgeBase == null) {
                sseEmitter.send(ChatSSEEvents.error("知识库不存在！"));
                return;
            }
            Integer aiVectorModelId = knowledgeBase.getAiVectorModelId();
            AiModel aiVectorModel = aiModelService.getById(aiVectorModelId);
            if (aiVectorModel == null) {
                sseEmitter.send(ChatSSEEvents.error("啊哦！当前知识库使用的的向量模型不存在！"));
                return;
            }
            List<Integer> docIds = knowledgeBaseDocService.listIdsByKbId(knowledgeBase.getId());
            embeddingModel = embeddingModelFactory.create(aiVectorModel);
            embeddingStore =
                    embeddingStoreFactory.create(embeddingModel.dimension());
            embeddingStoreContentRetriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .maxResults(5)
                    .minScore(0.65)
                    .filter(MetadataFilterBuilder.metadataKey("docId").isIn(docIds))
                    .build();
        }
        AiModel aiModel = aiModelService.getById(modelId);
        if (aiModel == null) {
            sseEmitter.send(ChatSSEEvents.error("请选择正确的模型！"));
            return;
        }
        // 创建流式聊天模型
        StreamingChatModel streamingChatModel = aiChatModelFactory.createStreamingChatModel(aiModel);

        AiServices<Assistant> aiServicesBuilder = AiServices
                .builder(Assistant.class)
                .streamingChatModel(streamingChatModel);
        String systemMessage = "你是一个问答助手，你的名字：AI 小助手；请根据用户提问使用中文回答问题，并尽量详细。" +
                "格式必须是正确 markdown 格式。";
        if (embeddingStoreContentRetriever != null) {
            aiServicesBuilder.contentRetriever(embeddingStoreContentRetriever);
            systemMessage = "你是一个知识库问答助手，你的名字：AI 小助手；请根据知识库内容回答问题。" +
                    "请使用中文回答，并尽量详细。格式必须是正确 markdown 格式。";
        }
        Assistant assistant = aiServicesBuilder
                .systemMessage(systemMessage)
                .chatMemory(MessageWindowChatMemory.builder()
                        .id(cid)
                        .maxMessages(PersistentChatMemoryStore.maxMessages)
                        .chatMemoryStore(persistentChatMemoryStore).build())
                .build();
        TokenStream stream = assistant.chat(text);
        List<Long> ids = initMsg(cid, text);// 插入聊天消息
        Long umId = ids.get(0);
        Long tmId = ids.get(1);
        Long cmId = ids.get(2);
        stream.onPartialThinking(partialThinking -> {
            String thinking = partialThinking.text();
            log.info("partial thinking: {}", partialThinking);
            try {
                sseEmitter.send(ChatSSEEvents.thinking(thinking).build());
                chatMessageService.appendThinkingMessage(tmId, thinking);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        stream.onPartialResponse(content -> {
            log.info("partial response: {}", content);
            try {
                sseEmitter.send(ChatSSEEvents.content(content).build());
                chatMessageService.appendContentMessage(cmId, content);
            } catch (IOException e) {
                sseEmitter.complete();
                throw new RuntimeException(e);
            }

        });
        stream.onError(throwable -> {
            try {
                log.error("error: ", throwable);
                sseEmitter.send(ChatSSEEvents.error("啊哦！聊天发生错误！"));
            } catch (IOException e) {
                sseEmitter.complete();
                throw new RuntimeException(e);
            }
        });
        stream.onCompleteResponse(chatResponse -> {
            try {
                sseEmitter.send(ChatSSEEvents.close("[DONE]").build());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            sseEmitter.complete();
            TokenUsage tokenUsage = chatResponse.tokenUsage();
            log.info("tokenUsage: {}", tokenUsage);
            ChatMessageMetadata chatMessageMetadata = new ChatMessageMetadata();
            chatMessageMetadata.setTokenUsage(tokenUsage);
            chatMessageService.updateMetadata(umId, chatMessageMetadata);
        });
        stream.start();
    }

    private List<Long> initMsg(Long cid, String text) {
        ChatMessageUser chatMessageUser = new ChatMessageUser();
        chatMessageUser.setCid(cid);
        chatMessageUser.setSender("USER");
        chatMessageUser.setContent(text);
        chatMessageUser.setContentType("TEXT");
        chatMessageUser.setSendAt(new Date());
        chatMessageUser.setMetadata(null);
        chatMessageService.addUserMessage(chatMessageUser);
        ChatMessageAiThinking chatMessageAiThinking = new ChatMessageAiThinking();
        chatMessageAiThinking.setCid(cid);
        chatMessageAiThinking.setMuId(chatMessageUser.getId());
        chatMessageAiThinking.setSender("AI");
        chatMessageAiThinking.setContent("");
        chatMessageAiThinking.setContentType("TEXT");
        chatMessageAiThinking.setSendAt(new Date());
        chatMessageAiThinking.setMetadata("{}");
        chatMessageService.addThinkingMessage(chatMessageAiThinking);
        ChatMessageAiContent chatMessageAiContent = new ChatMessageAiContent();
        chatMessageAiContent.setCid(cid);
        chatMessageAiContent.setMuId(chatMessageUser.getId());
        chatMessageAiContent.setSender("AI");
        chatMessageAiContent.setContent("");
        chatMessageAiContent.setContentType("TEXT");
        chatMessageAiContent.setSendAt(new Date());
        chatMessageAiContent.setMetadata("{}");
        chatMessageService.addContentMessage(chatMessageAiContent);
        return List.of(chatMessageUser.getId(), chatMessageAiThinking.getId(), chatMessageAiContent.getId());
    }

    interface Assistant {
        TokenStream chat(String userMessage);
    }

}
