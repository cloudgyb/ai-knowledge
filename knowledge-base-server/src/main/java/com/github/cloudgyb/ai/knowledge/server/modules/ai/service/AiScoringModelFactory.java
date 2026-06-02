package com.github.cloudgyb.ai.knowledge.server.modules.ai.service;

import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.model.scoring.onnx.OnnxScoringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 评分模型工厂类（用于提示词或者RAG检索重排序）
 *
 * @author cloudgyb
 * @since 2026/6/2 16:54
 */
@Component
public class AiScoringModelFactory {
    private static final Logger logger = LoggerFactory.getLogger(AiScoringModelFactory.class);
    private final Map<String, ScoringModel> scoringModelCache = new ConcurrentHashMap<>();
    @Value("${ai.scoring.model.onnx.pathToModel}")
    private String modelPath;

    @Value("${ai.scoring.model.onnx.pathToTokenizer}")
    private String tokenizerPath;

    public ScoringModel createInProcessModel() {
        return scoringModelCache.computeIfAbsent("InProcessModel", (key) -> doCreateOnnxScoringModel());
    }

    private OnnxScoringModel doCreateOnnxScoringModel() {
        if (logger.isDebugEnabled()) {
            logger.debug("创建Onnx评分模型,modelPath:{},tokenizerPath:{}", modelPath, tokenizerPath);
        }
        return new OnnxScoringModel(modelPath, tokenizerPath);
    }
}
