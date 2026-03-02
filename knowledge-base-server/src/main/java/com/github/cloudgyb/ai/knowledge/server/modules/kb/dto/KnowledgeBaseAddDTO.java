package com.github.cloudgyb.ai.knowledge.server.modules.kb.dto;

import com.github.cloudgyb.ai.knowledge.server.modules.kb.KnowledgeBaseType;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

/**
 * 知识库添加DTO
 *
 * @author cloudgyb
 * @since 2026/3/2 21:56
 */
public class KnowledgeBaseAddDTO {
    private Integer id;

    /**
     * 知识库名称
     */
    @NotBlank(message = "名称不能为空")
    @Length(max = 50, message = "名称长度不能超过50")
    private String name;

    /**
     * 知识库描述
     */
    private String remark;

    /**
     * AI 向量模型id，关联 ai_model 表
     */
    @NotBlank(message = "向量模型不能为空")
    private Integer aiVectorModelId;

    /**
     * 状态：0 禁用 1启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 类型，支持知识库和记忆库
     */
    @NotNull(message = "类型不能为空")
    private KnowledgeBaseType type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getAiVectorModelId() {
        return aiVectorModelId;
    }

    public void setAiVectorModelId(Integer aiVectorModelId) {
        this.aiVectorModelId = aiVectorModelId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public KnowledgeBaseType getType() {
        return type;
    }

    public void setType(KnowledgeBaseType type) {
        this.type = type;
    }

    public KnowledgeBase toKnowledgeBase() {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setName(this.name);
        kb.setRemark(this.remark);
        kb.setAiVectorModelId(this.aiVectorModelId);
        kb.setStatus(this.status);
        kb.setType(this.type.getType());
        kb.setCreateTime(new Date());
        return kb;
    }
}
