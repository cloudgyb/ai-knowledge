package com.github.cloudgyb.ai.knowledge.server.modules.kb.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.service.AiModelService;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBase;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.dto.KnowledgeBaseAddDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 个人知识库管理服务实现
 *
 * @author cloudgyb
 * @since 2026/2/28 15:17
 */
@Service
public class KnowledgeBaseManageService {
    private final AiModelService aiModelService;
    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseManageService(AiModelService aiModelService,
                                      KnowledgeBaseService knowledgeBaseService) {
        this.aiModelService = aiModelService;
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addKnowledgeBase(KnowledgeBaseAddDTO dto) {
        Integer aiVectorModelId = dto.getAiVectorModelId();

        validateAiVectorModelId(aiVectorModelId);

        KnowledgeBase knowledgeBase = dto.toKnowledgeBase();
        knowledgeBase.setCreateTime(new Date());
        knowledgeBaseService.save(knowledgeBase);
    }

    private void validateAiVectorModelId(Integer aiVectorModelId) {
        AiModel aiModel = aiModelService.getById(aiVectorModelId);
        if (aiModel != null) {
            return;
        }
        throw new RuntimeException("向量模型不存在");
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateKnowledgeBase(KnowledgeBaseAddDTO dto) {
        KnowledgeBase knowledgeBase = dto.toKnowledgeBase();
        knowledgeBase.setUpdateTime(new Date());
        knowledgeBaseService.updateById(knowledgeBase);
    }

    public Page<KnowledgeBase> page(Integer pageNum, Integer pageSize, String name) {
        Wrapper<KnowledgeBase> queryWrapper = StringUtils.isNotBlank(name) ?
                new LambdaQueryWrapper<KnowledgeBase>().like(KnowledgeBase::getName, name) : null;
        return knowledgeBaseService.page(new Page<>(pageNum, pageSize), queryWrapper);
    }
}
