package com.github.cloudgyb.ai.knowledge.server.modules.rag;

import com.github.cloudgyb.ai.knowledge.server.modules.ai.AIModelProviders;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModelConfig;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.service.AiModelConfigService;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.domain.SysAiModelProvider;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.service.SysAiModelProviderService;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.model.qianfan.QianfanEmbeddingModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * EmbeddingModel 工厂类
 *
 * @author cloudgyb
 * @since 2026/3/10 14:03
 */
@Component
public class EmbeddingModelFactory {
    private final SysAiModelProviderService sysAiModelProviderService;
    private final AiModelConfigService aiModelConfigService;

    public EmbeddingModelFactory(SysAiModelProviderService sysAiModelProviderService,
                                 AiModelConfigService aiModelConfigService) {
        this.sysAiModelProviderService = sysAiModelProviderService;
        this.aiModelConfigService = aiModelConfigService;
    }

    public EmbeddingModel create(AiModel aiModel) {
        Integer providerId = aiModel.getProviderId();
        SysAiModelProvider aiModelProvider;
        String providerCode = "AllMiniLmL6V2";

        if (providerId != 0) {
            aiModelProvider = sysAiModelProviderService.getById(providerId);
            if (aiModelProvider == null) {
                throw new RuntimeException("知识库AI向量模型提供商不存在！");
            }
            providerCode = aiModelProvider.getProviderCode();
        }
        AiModelConfig aiModelConfig = aiModelConfigService.getByModelId(aiModel.getId());
        // 根据模型提供商创建内嵌模型
        return switch (AIModelProviders.valueOf(providerCode)) {
            case OpenAI -> createOpenAIEmbeddingModel(aiModel, aiModelConfig);
            case TongYi -> createTongyiEmbeddingModel(aiModel, aiModelConfig);
            case ZhiPuAI -> createZhiPuAIEmbeddingModel(aiModel, aiModelConfig);
            case QianFan -> createQianFanEmbeddingModel(aiModel, aiModelConfig);
            case Ollama -> createOllamaEmbeddingModel(aiModel, aiModelConfig);
            default -> new AllMiniLmL6V2EmbeddingModel();
        };


    }

    private EmbeddingModel createOllamaEmbeddingModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return dev.langchain4j.model.ollama.OllamaEmbeddingModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .timeout(Duration.ofSeconds(aiModelConfig.getTimeout()))
                .modelName(aiModel.getModelName())
                .build();
    }

    private EmbeddingModel createQianFanEmbeddingModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return QianfanEmbeddingModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .modelName(aiModel.getModelName())
                .secretKey(aiModel.getModelApiSecret())
                .build();
    }

    private EmbeddingModel createZhiPuAIEmbeddingModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return ZhipuAiEmbeddingModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .model(aiModel.getModelName())
                .dimensions(1536)
                .build();
    }

    private EmbeddingModel createTongyiEmbeddingModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        return QwenEmbeddingModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .modelName(aiModel.getModelName())
                .dimension(1024)
                .build();
    }

    private EmbeddingModel createOpenAIEmbeddingModel(AiModel aiModel, AiModelConfig aiModelConfig) {
        Integer timeout = aiModelConfig.getTimeout();
        return OpenAiEmbeddingModel.builder()
                .baseUrl(aiModel.getModelUrl())
                .apiKey(aiModel.getModelApiKey())
                .modelName(aiModel.getModelName())
                .timeout(Duration.ofSeconds(timeout))
                .build();
    }
}
