package com.github.cloudgyb.ai.knowledge.server.modules.kb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.ApiResponse;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.validation.Group;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBase;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.dto.KnowledgeBaseAddDTO;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.service.KnowledgeBaseManageService;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人知识库管理
 *
 * @author cloudgyb
 * @since 2026/2/28 15:17
 */
@RestController
@RequestMapping("/kb")
@Validated
public class KnowledgeBaseManageController {
    private final KnowledgeBaseManageService knowledgeBaseManageService;

    public KnowledgeBaseManageController(KnowledgeBaseManageService knowledgeBaseManageService) {
        this.knowledgeBaseManageService = knowledgeBaseManageService;
    }

    public ApiResponse<Page<KnowledgeBase>> getKnowledgeBaseList(@Param("pageNum") Integer pageNum,
                                                                 @Param("pageSize") Integer pageSize,
                                                                 @Param("name") String name) {
        Page<KnowledgeBase> page = knowledgeBaseManageService.page(pageNum, pageSize, name);
        return ApiResponse.success(page);
    }

    @PostMapping("/add")
    public ApiResponse<Void> createKnowledgeBase(@RequestBody KnowledgeBaseAddDTO dto) {
        knowledgeBaseManageService.addKnowledgeBase(dto);
        return ApiResponse.success();
    }

    @PostMapping("/update")
    public ApiResponse<Void> updateKnowledgeBase(@Validated(Group.Update.class)
                                                 @RequestBody KnowledgeBaseAddDTO dto) {
        knowledgeBaseManageService.updateKnowledgeBase(dto);
        return ApiResponse.success();
    }
}
