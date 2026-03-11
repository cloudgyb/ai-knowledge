package com.github.cloudgyb.ai.knowledge.server.modules.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.AiModelType;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.service.AiModelService;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.ApiResponse;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.dto.AntDesignSelectOption;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI模型管理
 *
 * @author cloudgyb
 * @since 2026/3/11 16:35
 */
@RestController
@RequestMapping("/ai/model")
public class AiModelController {
    private final AiModelService aiModelService;

    public AiModelController(AiModelService aiModelService) {
        this.aiModelService = aiModelService;
    }

    @GetMapping("/types")
    public ApiResponse<AntDesignSelectOption[]> aiModelTypes() {
        AiModelType[] values = AiModelType.values();
        AntDesignSelectOption[] antDesignSelectOptions = new AntDesignSelectOption[values.length];
        for (AiModelType value : values) {
            antDesignSelectOptions[value.ordinal()] = new AntDesignSelectOption(value.name(), value.getDesc());
        }
        return ApiResponse.success(antDesignSelectOptions);
    }

    @GetMapping
    public ApiResponse<Page<AiModel>> page(Integer pageNum, Integer pageSize, String name) {
        Page<AiModel> page = aiModelService.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<AiModel>().like(AiModel::getCustomName, name));
        return ApiResponse.success(page);
    }
}
