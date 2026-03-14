package com.github.cloudgyb.ai.knowledge.server.modules.kb.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 用户自定义的知识库存储表
 *
 * @author cloudgyb
 * @since 2026-03-02 16:59:29
 */
@Getter
@Setter
@TableName(value = "knowledge_base")
public class KnowledgeBase {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 知识库名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 知识库描述
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * AI 向量模型id，关联 ai_model 表
     */
    @TableField(value = "ai_vector_model_id")
    private Integer aiVectorModelId;

    @TableField(exist = false)
    private AiModel aiVectorModel;

    /**
     * 状态：0 禁用 1启用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 类型，支持知识库和记忆库
     */
    @TableField(value = "type")
    private String type;

    /**
     *
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     *
     */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private Date updateTime;
}