package com.github.cloudgyb.ai.knowledge.server.modules.ai.mapper;

import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【ai_model(系统支持的 ai 模型存储表)】的数据库操作Mapper
 *
 * @author cloudgyb
 * @since 2026-02-28 15:01:50
 */
@Mapper
public interface AiModelMapper extends BaseMapper<AiModel> {

}




