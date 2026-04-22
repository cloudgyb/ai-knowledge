package com.github.cloudgyb.ai.knowledge.server.modules.ai.service;

import com.github.cloudgyb.ai.knowledge.server.modules.ai.AIModelProviders;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModelConfig;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.BusinessException;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.domain.SysAiModelProvider;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.service.SysAiModelProviderService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.community.model.qianfan.QianfanChatModel;
import dev.langchain4j.community.model.qianfan.QianfanStreamingChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ai Chat Model 工厂类
 * <p>根据模型 id 缓存 AI 模型对象</p>
 *
 * @author cloudgyb
 * @since 2026/04/22 10:53
 */
@Component
public class AiChatModelFactory {
    private final SysAiModelProviderService sysAiModelProviderService;
    private final AiModelConfigService aiModelConfigService;
    private final ConcurrentHashMap<Integer, ChatModel> aiModelCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, StreamingChatModel> aiStreamingModelCache = new ConcurrentHashMap<>();

    public AiChatModelFactory(SysAiModelProviderService sysAiModelProviderService,
                              AiModelConfigService aiModelConfigService) {
        this.sysAiModelProviderService = sysAiModelProviderService;
        this.aiModelConfigService = aiModelConfigService;
    }

    public StreamingChatModel createStreamingChatModel(AiModel aiModel) {
        Integer aiModelId = aiModel.getId();
        return aiStreamingModelCache.computeIfAbsent(aiModelId, modelId -> doCreateStreamingChatModel(aiModel));
    }

    private StreamingChatModel doCreateStreamingChatModel(AiModel aiModel) {
        Integer providerId = aiModel.getProviderId();
        SysAiModelProvider aiModelProvider;
        String providerCode;
        aiModelProvider = sysAiModelProviderService.getById(providerId);
        if (aiModelProvider == null) {
            throw new BusinessException("知识库AI向量模型提供商不存在！");
        }
        providerCode = aiModelProvider.getProviderCode();

        AiModelConfig aiModelConfig = aiModelConfigService.getByModelId(aiModel.getId());
        // 根据模型提供商创建内嵌模型
        return switch (AIModelProviders.valueOf(providerCode)) {
            case OpenAI -> createOpenAIStreamingChatModel(aiModel, aiModelConfig);
            case TongYi -> createTongyiAIStreamingChatModel(aiModel, aiModelConfig);
            case ZhiPuAI -> createZhiPuAIStreamingChatModel(aiModel, aiModelConfig);
            case QianFan -> createQianFanAIStreamingChatModel(aiModel, aiModelConfig);
            case Ollama -> createOllamaAIStreamingChatModel(aiModel, aiModelConfig);
            case Claude -> createClaudeAIStreamingChatModel(aiModel, aiModelConfig);
            default -> throw new BusinessException("不支持的模型提供商！");
        };
    }

    private StreamingChatModel createClaudeAIStreamingChatModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return AnthropicStreamingChatModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .modelName(aiModel.getModelName())
                .temperature(aiModelConfig.getTemperature())
                .topP(aiModelConfig.getTalk())
                .maxTokens(aiModelConfig.getTokens())
                .timeout(Duration.ofSeconds(aiModelConfig.getTimeout()))
                .build();
    }

    private StreamingChatModel createOllamaAIStreamingChatModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return dev.langchain4j.model.ollama.OllamaStreamingChatModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .timeout(Duration.ofSeconds(aiModelConfig.getTimeout()))
                .modelName(aiModel.getModelName())
                .temperature(aiModelConfig.getTemperature())
                .topP(aiModelConfig.getTalk())
                .build();
    }

    private StreamingChatModel createQianFanAIStreamingChatModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return QianfanStreamingChatModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .modelName(aiModel.getModelName())
                .secretKey(aiModel.getModelApiSecret())
                .temperature(aiModelConfig.getTemperature())
                .topP(aiModelConfig.getTalk())
                .maxOutputTokens(aiModelConfig.getTokens())
                .build();
    }

    private StreamingChatModel createZhiPuAIStreamingChatModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return ZhipuAiStreamingChatModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .model(aiModel.getModelName())
                .maxToken(aiModelConfig.getTokens())
                .temperature(aiModelConfig.getTemperature())
                .topP(aiModelConfig.getTalk())
                .build();
    }

    private StreamingChatModel createTongyiAIStreamingChatModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return QwenStreamingChatModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .modelName(aiModel.getModelName())
                .topP(aiModelConfig.getTalk())
                .maxTokens(aiModelConfig.getTokens())
                .temperature(aiModelConfig.getTemperature().floatValue())
                .isMultimodalModel(false)
                .build();
    }

    private StreamingChatModel createOpenAIStreamingChatModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        Integer timeout = aiModelConfig.getTimeout();
        return OpenAiStreamingChatModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .modelName(aiModel.getModelName())
                .timeout(Duration.ofSeconds(timeout))
                .maxTokens(aiModelConfig.getTokens())
                .temperature(aiModelConfig.getTemperature())
                .topP(aiModelConfig.getTalk())
                .build();
    }

    public ChatModel createChatModel(AiModel aiModel) {
        Integer aiModelId = aiModel.getId();
        return aiModelCache.computeIfAbsent(aiModelId, modelId -> doCreateChatModel(aiModel));
    }

    private ChatModel doCreateChatModel(AiModel aiModel) {
        Integer providerId = aiModel.getProviderId();
        SysAiModelProvider aiModelProvider;
        String providerCode;
        aiModelProvider = sysAiModelProviderService.getById(providerId);
        if (aiModelProvider == null) {
            throw new BusinessException("知识库AI向量模型提供商不存在！");
        }
        providerCode = aiModelProvider.getProviderCode();

        AiModelConfig aiModelConfig = aiModelConfigService.getByModelId(aiModel.getId());
        // 根据模型提供商创建内嵌模型
        return switch (AIModelProviders.valueOf(providerCode)) {
            case OpenAI -> createOpenAIChatModel(aiModel, aiModelConfig);
            case TongYi -> createTongyiAIChatModel(aiModel, aiModelConfig);
            case ZhiPuAI -> createZhiPuAIChatModel(aiModel, aiModelConfig);
            case QianFan -> createQianFanAIChatModel(aiModel, aiModelConfig);
            case Ollama -> createOllamaAIChatModel(aiModel, aiModelConfig);
            case Claude -> createClaudeAIChatModel(aiModel, aiModelConfig);
            default -> throw new BusinessException("不支持的模型提供商！");
        };
    }

    private ChatModel createClaudeAIChatModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return AnthropicChatModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .modelName(aiModel.getModelName())
                .temperature(aiModelConfig.getTemperature())
                .topP(aiModelConfig.getTalk())
                .maxTokens(aiModelConfig.getTokens())
                .timeout(Duration.ofSeconds(aiModelConfig.getTimeout()))
                .build();
    }

    private ChatModel createOllamaAIChatModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return dev.langchain4j.model.ollama.OllamaChatModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .timeout(Duration.ofSeconds(aiModelConfig.getTimeout()))
                .modelName(aiModel.getModelName())
                .temperature(aiModelConfig.getTemperature())
                .topP(aiModelConfig.getTalk())
                .build();
    }

    private ChatModel createQianFanAIChatModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return QianfanChatModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .modelName(aiModel.getModelName())
                .secretKey(aiModel.getModelApiSecret())
                .temperature(aiModelConfig.getTemperature())
                .topP(aiModelConfig.getTalk())
                .maxOutputTokens(aiModelConfig.getTokens())
                .build();
    }

    private ChatModel createZhiPuAIChatModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return ZhipuAiChatModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .model(aiModel.getModelName())
                .maxToken(aiModelConfig.getTokens())
                .temperature(aiModelConfig.getTemperature())
                .topP(aiModelConfig.getTalk())
                .build();
    }

    private ChatModel createTongyiAIChatModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return QwenChatModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .modelName(aiModel.getModelName())
                .topP(aiModelConfig.getTalk())
                .maxTokens(aiModelConfig.getTokens())
                .temperature(aiModelConfig.getTemperature().floatValue())
                .isMultimodalModel(false)
                .build();
    }

    private ChatModel createOpenAIChatModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        Integer timeout = aiModelConfig.getTimeout();
        return OpenAiChatModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .modelName(aiModel.getModelName())
                .timeout(Duration.ofSeconds(timeout))
                .maxTokens(aiModelConfig.getTokens())
                .temperature(aiModelConfig.getTemperature())
                .topP(aiModelConfig.getTalk())
                .build();
    }

    public void removeCacheByAiModelId(Integer aiModelId) {
        aiStreamingModelCache.remove(aiModelId);
        aiModelCache.remove(aiModelId);
    }

    @PreDestroy
    public void destroy() {
        aiStreamingModelCache.clear();
        aiModelCache.clear();
    }
}
