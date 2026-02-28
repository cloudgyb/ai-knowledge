package com.github.cloudgyb.ai.knowledge.server.modules.ai.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.mapper.AiModelMapper;
import org.springframework.stereotype.Service;

/**
 * 针对表【ai_model(系统支持的 ai 模型存储表)】的数据库操作Service实现
 *
 * @author cloudgyb
 * @since 2026-02-28 15:01:50
 */
@Service
public class AiModelService extends ServiceImpl<AiModelMapper, AiModel> {

}




