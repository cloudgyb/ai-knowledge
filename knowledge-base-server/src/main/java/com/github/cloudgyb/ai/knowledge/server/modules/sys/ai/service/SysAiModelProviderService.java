package com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.AiModelType;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.domain.SysAiModelProvider;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.mapper.SysAiModelProviderMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 模型提供商 服务
 *
 * @author cloudgyb
 * @since 2026-02-28 17:07:05
 */
@Service
public class SysAiModelProviderService extends ServiceImpl<SysAiModelProviderMapper, SysAiModelProvider> {

    public List<SysAiModelProvider> getAllProviders() {
        return this.list();
    }

    public List<SysAiModelProvider> getProvidersByModelType(AiModelType modelType) {
        return this.baseMapper.getProvidersByModelType(modelType.name());
    }
}