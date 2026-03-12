package com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.AiModelType;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.ApiResponse;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.domain.SysAiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.domain.SysAiModelProvider;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.service.SysAiModelProviderService;
import com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.service.SysAiModelService;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 * 系统提供的AI模型相关接口
 *
 * @author cloudgyb
 * @since 2026/3/3 10:37
 */
@RestController
@RequestMapping("/sys/ai/model")
public class SysAiModelController {
    private final SysAiModelProviderService sysAiModelProviderService;
    private final SysAiModelService sysAiModelService;

    public SysAiModelController(SysAiModelProviderService sysAiModelProviderService,
                                SysAiModelService sysAiModelService) {
        this.sysAiModelProviderService = sysAiModelProviderService;
        this.sysAiModelService = sysAiModelService;
    }

    @GetMapping("/providers")
    public ApiResponse<List<SysAiModelProvider>> getAllProviders(@Param("modelType") AiModelType modelType) {
        if (modelType != null)
            return ApiResponse.success(sysAiModelProviderService.getProvidersByModelType(modelType));
        return ApiResponse.success(sysAiModelProviderService.getAllProviders());
    }

    @GetMapping("/list")
    public ApiResponse<List<SysAiModel>> list(@Param("providerId") Integer providerId) {
        List<SysAiModel> list = sysAiModelService.list(new LambdaQueryWrapper<SysAiModel>()
                .eq(SysAiModel::getProviderId, providerId));
        return ApiResponse.success(list);
    }

}
