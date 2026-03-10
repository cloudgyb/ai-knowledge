package com.github.cloudgyb.ai.knowledge.server.modules.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModelConfig;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.mapper.AiModelConfigMapper;
import org.springframework.stereotype.Service;

/**
 * 针对表【ai_model_config(用户自定义的 AI 模型高级配置，包含模型温度、最大回复等)】的数据库操作Service实现
 *
 * @author cloudgyb
 * @since 2026-02-28 15:38:56
 */
@Service
public class AiModelConfigService extends ServiceImpl<AiModelConfigMapper, AiModelConfig> {

    public AiModelConfig getByModelId(Integer modelId) {
        return getOne(new LambdaQueryWrapper<AiModelConfig>()
                .eq(AiModelConfig::getModelId, modelId).last("LIMIT 1"));
    }
}




