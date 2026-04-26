package com.github.cloudgyb.ai.knowledge.server.modules.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.AiModelType;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModelConfig;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.dto.AiModelDTO;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.mapper.AiModelMapper;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.BusinessException;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBase;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.service.KnowledgeBaseService;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.domain.SysAiModelProvider;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.service.SysAiModelProviderService;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 针对表【ai_model(系统支持的 ai 模型存储表)】的数据库操作Service实现
 *
 * @author cloudgyb
 * @since 2026-02-28 15:01:50
 */
@Service
public class AiModelService extends ServiceImpl<AiModelMapper, AiModel> {
    private final AiModelConfigService aiModelConfigService;
    private final SysAiModelProviderService sysAiModelProviderService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final AiChatModelFactory aiChatModelFactory;


    public AiModelService(AiModelConfigService aiModelConfigService,
                          SysAiModelProviderService sysAiModelProviderService,
                          KnowledgeBaseService knowledgeBaseService,
                          AiChatModelFactory aiChatModelFactory) {
        this.aiModelConfigService = aiModelConfigService;
        this.sysAiModelProviderService = sysAiModelProviderService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.aiChatModelFactory = aiChatModelFactory;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addAiModel(AiModelDTO dto) {
        Integer providerId = dto.getProviderId();
        SysAiModelProvider byId = sysAiModelProviderService.getById(providerId);
        if (byId == null) {
            throw new BusinessException("AI模型提供商不存在");
        }
        String customName = dto.getCustomName();
        AiModel one = getOne(new LambdaQueryWrapper<AiModel>().eq(AiModel::getCustomName, customName).last("LIMIT 1"));
        if (one != null) {
            throw new BusinessException("模型名称已存在");
        }
        AiModel aiModel = new AiModel();
        aiModel.setCustomName(dto.getCustomName());
        aiModel.setModelName(dto.getModelName());
        aiModel.setModelType(dto.getModelType().name());
        aiModel.setModelUrl(dto.getModelUrl());
        aiModel.setModelApiKey(dto.getModelApiKey());
        aiModel.setModelApiSecret(dto.getModelApiSecret());
        aiModel.setProviderId(providerId);
        aiModel.setStatus(dto.getStatus());
        aiModel.setCreateUserId(1L);
        boolean save = save(aiModel);
        if (!save) {
            throw new BusinessException("添加模型失败");
        }
        AiModelConfig config = dto.getConfig();
        config.setModelId(aiModel.getId());
        boolean save1 = aiModelConfigService.save(config);
        if (!save1) {
            throw new BusinessException("模型配置保存失败");
        }
    }

    public Page<AiModelDTO> page(@NotNull Integer pageNum, @NotNull Integer pageSize,
                                 String name, String type, Boolean enable) {
        LambdaQueryWrapper<AiModel> like = new LambdaQueryWrapper<AiModel>()
                .eq(StringUtils.isNotBlank(type), AiModel::getModelType, type)
                .eq(enable != null, AiModel::getStatus, Boolean.TRUE.equals(enable) ? 1 : 0)
                .like(StringUtils.isNotBlank(name), AiModel::getCustomName, name);
        Page<AiModel> page = page(new Page<>(pageNum, pageSize), like);
        List<AiModel> records = page.getRecords();
        List<AiModelDTO> dtos = records.stream().map(aiModel -> {
            AiModelDTO aiModelDTO = new AiModelDTO();
            BeanUtils.copyProperties(aiModel, aiModelDTO);
            aiModelDTO.setModelType(AiModelType.valueOf(aiModel.getModelType()));
            return aiModelDTO;
        }).peek(aiModel -> {
            aiModel.setConfig(aiModelConfigService.getByModelId(aiModel.getId()));
            aiModel.setProvider(sysAiModelProviderService.getById(aiModel.getProviderId()));
        }).toList();
        Page<AiModelDTO> aiModelDTOPage = new Page<>();
        BeanUtils.copyProperties(page, aiModelDTOPage, "records");
        aiModelDTOPage.setRecords(dtos);
        return aiModelDTOPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(@NotNull Integer id) {
        AiModel aiModel = getById(id);
        if (aiModel == null) {
            throw new BusinessException("模型不存在");
        }
        LambdaQueryWrapper<KnowledgeBase> queryWrapper = new LambdaQueryWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getAiVectorModelId, aiModel.getId());
        boolean exists = knowledgeBaseService.exists(queryWrapper);
        if (exists) {
            throw new BusinessException("模型正在被使用，无法删除");
        }

        boolean remove = removeById(id);
        if (!remove) {
            throw new BusinessException("删除模型失败");
        }
        boolean b = aiModelConfigService.removeByModelId(id);
        if (!b) {
            throw new BusinessException("删除模型配置失败");
        }

    }

    public void updateAiModel(AiModelDTO dto) {
        AiModel aiModel = getById(dto.getId());
        if (aiModel == null) {
            throw new BusinessException("AI模型不存在");
        }
        LambdaQueryWrapper<KnowledgeBase> queryWrapper = new LambdaQueryWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getAiVectorModelId, aiModel.getId());
        boolean exists = knowledgeBaseService.exists(queryWrapper);
        if (exists && !aiModel.getModelType().equals(dto.getModelType().name())) {
            throw new BusinessException("模型正在被使用，无法修改模型类型！");
        }
        BeanUtils.copyProperties(dto, aiModel);
        aiModel.setId(dto.getId());
        boolean b = updateById(aiModel);
        if (!b) {
            throw new BusinessException("更新AI模型失败");
        }
        AiModelConfig modelConfig = aiModelConfigService.getByModelId(aiModel.getId());
        Integer id = modelConfig.getId();
        Integer modelId = modelConfig.getModelId();
        AiModelConfig config = dto.getConfig();
        BeanUtils.copyProperties(config, modelConfig);
        modelConfig.setId(id);
        modelConfig.setModelId(modelId);
        boolean update = aiModelConfigService.updateById(modelConfig);
        if (!update) {
            throw new BusinessException("更新AI模型配置失败");
        } else {
            aiChatModelFactory.removeCacheByAiModelId(dto.getId());
        }
    }
}




