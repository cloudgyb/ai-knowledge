package com.github.cloudgyb.ai.knowledge.server.modules.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.AiModelType;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModelConfig;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.dto.AiModelDTO;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.mapper.AiModelMapper;
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

    public AiModelService(AiModelConfigService aiModelConfigService,
                          SysAiModelProviderService sysAiModelProviderService) {
        this.aiModelConfigService = aiModelConfigService;
        this.sysAiModelProviderService = sysAiModelProviderService;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addAiModel(AiModelDTO dto) {
        Integer providerId = dto.getProviderId();
        SysAiModelProvider byId = sysAiModelProviderService.getById(providerId);
        if (byId == null) {
            throw new RuntimeException("AI模型提供商不存在");
        }
        String customName = dto.getCustomName();
        AiModel one = getOne(new LambdaQueryWrapper<AiModel>().eq(AiModel::getCustomName, customName).last("LIMIT 1"));
        if (one != null) {
            throw new RuntimeException("模型名称已存在");
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
            throw new RuntimeException("添加模型失败");
        }
        AiModelConfig config = dto.getConfig();
        config.setModelId(aiModel.getId());
        boolean save1 = aiModelConfigService.save(config);
        if (!save1) {
            throw new RuntimeException("模型配置保存失败");
        }
    }

    public Page<AiModelDTO> page(@NotNull Integer pageNum, @NotNull Integer pageSize,
                                 String name, String type) {
        LambdaQueryWrapper<AiModel> like = new LambdaQueryWrapper<AiModel>()
                .eq(StringUtils.isNotBlank(type), AiModel::getModelType, type)
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
}




