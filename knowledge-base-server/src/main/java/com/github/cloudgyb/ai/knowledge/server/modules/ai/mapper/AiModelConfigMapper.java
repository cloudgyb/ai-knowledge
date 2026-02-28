package com.github.cloudgyb.ai.knowledge.server.modules.ai.mapper;

import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModelConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【ai_model_config(用户自定义的 AI 模型高级配置，包含模型温度、最大回复等)】的数据库操作Mapper
 *
 * @author cloudgyb
 * @since 2026-02-28 15:38:56
 */
@Mapper
public interface AiModelConfigMapper extends BaseMapper<AiModelConfig> {

}




