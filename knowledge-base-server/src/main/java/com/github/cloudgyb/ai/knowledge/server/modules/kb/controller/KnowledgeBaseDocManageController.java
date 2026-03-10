package com.github.cloudgyb.ai.knowledge.server.modules.kb.controller;

import com.github.cloudgyb.ai.knowledge.server.modules.commons.ApiResponse;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.service.KnowledgeBaseDocService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人知识库关联的文档管理
 *
 * @author cloudgyb
 * @since 2026/03/03 11:09
 */
@RestController
@RequestMapping("/kb/doc")
@Validated
public class KnowledgeBaseDocManageController {
    private final KnowledgeBaseDocService knowledgeBaseDocService;

    public KnowledgeBaseDocManageController(KnowledgeBaseDocService knowledgeBaseDocService) {
        this.knowledgeBaseDocService = knowledgeBaseDocService;
    }

    @PostMapping("/add")
    public ApiResponse<Integer> add(@NotNull @RequestParam("kbId") Integer kbId,
                                    @NotBlank @RequestParam("title") String title,
                                    @NotNull @RequestParam("file") MultipartFile file) {
        int docId = knowledgeBaseDocService.addDoc(kbId, title, file);
        return ApiResponse.success(docId);
    }

    @PostMapping("/update")
    public ApiResponse<Void> update(@NotNull @RequestParam("id") Integer id,
                                    @NotNull @RequestParam("file") String title,
                                    @NotNull @RequestParam("enable") Boolean enable,
                                    @RequestParam("file") MultipartFile file) {
        knowledgeBaseDocService.updateDoc(id, title, enable, file);
        return ApiResponse.success();
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@NotNull @RequestParam("id") Integer id) {
        knowledgeBaseDocService.delDoc(id);
        return ApiResponse.success();
    }
}
