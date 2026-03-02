package com.github.cloudgyb.ai.knowledge.server.modules.kb.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 用户自定义的知识库存储表
 *
 * @author cloudgyb
 * @since 2026-03-02 16:59:29
 */
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
    @TableField(value = "create_time")
    private Date createTime;

    /**
     *
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     *
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 知识库名称
     */
    public String getName() {
        return name;
    }

    /**
     * 知识库名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 知识库描述
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 知识库描述
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * AI 向量模型id，关联 ai_model 表
     */
    public Integer getAiVectorModelId() {
        return aiVectorModelId;
    }

    /**
     * AI 向量模型id，关联 ai_model 表
     */
    public void setAiVectorModelId(Integer aiVectorModelId) {
        this.aiVectorModelId = aiVectorModelId;
    }

    /**
     * 状态：0 禁用 1启用
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 状态：0 禁用 1启用
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 类型，支持知识库和记忆库
     */
    public String getType() {
        return type;
    }

    /**
     * 类型，支持知识库和记忆库
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     *
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     *
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     *
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}