package com.github.cloudgyb.ai.knowledge.server.modules.kb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.AiModelType;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.service.AiModelService;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.BusinessException;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.KnowledgeBaseType;
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
    private final KnowledgeBaseDocService knowledgeBaseDocService;

    public KnowledgeBaseManageService(AiModelService aiModelService,
                                      KnowledgeBaseService knowledgeBaseService,
                                      KnowledgeBaseDocService knowledgeBaseDocService) {
        this.aiModelService = aiModelService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.knowledgeBaseDocService = knowledgeBaseDocService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addKnowledgeBase(KnowledgeBaseAddDTO dto) {
        Integer aiVectorModelId = dto.getAiVectorModelId();

        validateAiVectorModelId(aiVectorModelId);

        KnowledgeBase knowledgeBase = dto.toKnowledgeBase();
        knowledgeBaseService.save(knowledgeBase);
    }

    private void validateAiVectorModelId(Integer aiVectorModelId) {
        AiModel aiModel = aiModelService.getById(aiVectorModelId);
        if (aiModel == null) {
            throw new BusinessException("向量模型不存在");
        }
        String modelType = aiModel.getModelType();
        if (!AiModelType.VECTOR.name().equals(modelType)) {
            throw new BusinessException("向量模型类型错误");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateKnowledgeBase(KnowledgeBaseAddDTO dto) {
        KnowledgeBase knowledgeBase = dto.toKnowledgeBase();
        knowledgeBase.setUpdateTime(new Date());
        knowledgeBaseService.updateById(knowledgeBase);
    }

    public Page<KnowledgeBase> page(Integer pageNum, Integer pageSize, KnowledgeBaseType type, String name) {
        LambdaQueryWrapper<KnowledgeBase> queryWrapper = new LambdaQueryWrapper<KnowledgeBase>()
                .eq(type != null, KnowledgeBase::getType, type)
                .like(StringUtils.isNotBlank(name), KnowledgeBase::getName, name);
        Page<KnowledgeBase> page = knowledgeBaseService.page(new Page<>(pageNum, pageSize), queryWrapper);
        page.getRecords().forEach(kb ->
                kb.setAiVectorModel(aiModelService.getById(kb.getAiVectorModelId())));
        return page;
    }

    public void deleteKnowledgeBase(Integer id) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.getById(id);
        if (knowledgeBase == null) {
            throw new BusinessException("知识库不存在");
        }
        knowledgeBaseDocService.delDocByKbId(id);
    }
}
