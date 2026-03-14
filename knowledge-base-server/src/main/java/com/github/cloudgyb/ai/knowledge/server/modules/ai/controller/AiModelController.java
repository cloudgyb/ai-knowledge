package com.github.cloudgyb.ai.knowledge.server.modules.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.AiModelType;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.dto.AiModelDTO;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.service.AiModelService;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.ApiResponse;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.dto.AntDesignSelectOption;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.validation.Group;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * AI模型管理
 *
 * @author cloudgyb
 * @since 2026/3/11 16:35
 */
@RestController
@RequestMapping("/ai/model")
@Validated
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
    public ApiResponse<Page<AiModelDTO>> page(@NotNull Integer pageNum,
                                              @NotNull Integer pageSize,
                                              String name, String type) {
        Page<AiModelDTO> page = aiModelService.page(pageNum, pageSize, name, type);
        return ApiResponse.success(page);
    }

    @PostMapping
    public ApiResponse<Void> add(@Validated(Group.Add.class) @RequestBody AiModelDTO dto) {
        aiModelService.addAiModel(dto);
        return ApiResponse.success();
    }
    @PutMapping
    public ApiResponse<Void> update(@Validated(Group.Update.class) @RequestBody AiModelDTO dto) {
        aiModelService.updateAiModel(dto);
        return ApiResponse.success();
    }


    @DeleteMapping
    public ApiResponse<Void> delete(@NotNull Integer id) {
        aiModelService.delete(id);
        return ApiResponse.success();
    }
}
