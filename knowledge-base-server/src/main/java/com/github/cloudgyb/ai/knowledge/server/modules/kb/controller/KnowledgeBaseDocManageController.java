package com.github.cloudgyb.ai.knowledge.server.modules.kb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.ApiResponse;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBaseDoc;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.service.KnowledgeBaseDocService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("/list")
    public ApiResponse<Page<KnowledgeBaseDoc>> list(@NotNull @RequestParam("kbId") Integer kbId,
                                                    @RequestParam("title") String title,
                                                    @RequestParam @NotNull Integer pageNum,
                                                    @RequestParam @NotNull Integer pageSize) {
        Page<KnowledgeBaseDoc> docs = knowledgeBaseDocService.page(kbId, title, pageNum, pageSize);
        return ApiResponse.success(docs);
    }

    @PostMapping("/add")
    public ApiResponse<List<Integer>> add(@NotNull @RequestParam("kbId") Integer kbId,
                                          @NotBlank @RequestParam("title") String title,
                                          @NotNull @RequestParam("files") MultipartFile[] files) {
        List<Integer> docIds = knowledgeBaseDocService.addDoc(kbId, title, files);
        return ApiResponse.success(docIds);
    }

    @PostMapping("/update")
    public ApiResponse<Void> update(@NotNull @RequestParam("id") Integer id,
                                    @NotNull @RequestParam("file") String title,
                                    @NotNull @RequestParam("enable") Boolean enable,
                                    @RequestParam("file") MultipartFile file) {
        knowledgeBaseDocService.updateDoc(id, title, enable, file);
        return ApiResponse.success();
    }

    @DeleteMapping()
    public ApiResponse<Void> delete(@NotNull @RequestParam("id") Integer id) {
        knowledgeBaseDocService.delDoc(id);
        return ApiResponse.success();
    }

    /**
     * 命中测试
     *
     * @param kbId 知识库id
     */
    @PostMapping("/test")
    public ApiResponse<Void> test(@NotNull @RequestParam("kbId") Integer kbId,
                                  @NotBlank @RequestParam("text") String text) {
        knowledgeBaseDocService.testDoc(kbId, text);
        return ApiResponse.success();
    }
}
