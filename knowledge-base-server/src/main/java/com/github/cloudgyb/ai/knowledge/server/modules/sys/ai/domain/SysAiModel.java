package com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 系统支持的 ai 模型存储表
 *
 * @author cloudgyb
 * @since 2026-02-28 14:09
 */
@TableName(value = "sys_ai_model")
public class SysAiModel {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 模型名称
     */
    @TableField(value = "model_name")
    private String modelName;

    /**
     * 模型类型
     */
    @TableField(value = "model_type")
    private String modelType;

    /**
     * 模型提供商
     */
    @TableField(value = "provider_id")
    private Integer providerId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "deleted")
    private Integer deleted;

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
     * 模型名称
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * 模型名称
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * 模型类型
     */
    public String getModelType() {
        return modelType;
    }

    /**
     * 模型类型
     */
    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    /**
     * 模型提供商
     */
    public Integer getProviderId() {
        return providerId;
    }

    /**
     * 模型提供商
     */
    public void setProviderId(Integer providerId) {
        this.providerId = providerId;
    }

    /**
     * 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 是否删除
     */
    public Integer getDeleted() {
        return deleted;
    }

    /**
     * 是否删除
     */
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}