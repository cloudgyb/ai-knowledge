package com.github.cloudgyb.ai.knowledge.server.modules.ai.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * AI 模型配置存储表
 *
 * @author cloudgyb
 * @since 2026-02-28 15:01:50
 */
@TableName(value = "ai_model")
public class AiModel {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 自定义名称
     */
    @TableField(value = "custom_name")
    private String customName;

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
     * 模型API URL
     */
    @TableField(value = "model_url")
    private String modelUrl;

    /**
     * 模型API Key
     */
    @TableField(value = "model_api_key")
    private String modelApiKey;

    /**
     * 模型API密钥
     */
    @TableField(value = "model_api_secret")
    private String modelApiSecret;

    /**
     * 模型提供商
     */
    @TableField(value = "provider_id")
    private Integer providerId;

    /**
     * 创建者id
     */
    @TableField(value = "create_user_id")
    private Long createUserId;

    /**
     * 模型状态，0未激活1已激活
     */
    @TableField(value = "status")
    private Integer status;

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
     * 自定义名称
     */
    public String getCustomName() {
        return customName;
    }

    /**
     * 自定义名称
     */
    public void setCustomName(String customName) {
        this.customName = customName;
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
     * 模型API URL
     */
    public String getModelUrl() {
        return modelUrl;
    }

    /**
     * 模型API URL
     */
    public void setModelUrl(String modelUrl) {
        this.modelUrl = modelUrl;
    }

    /**
     * 模型API Key
     */
    public String getModelApiKey() {
        return modelApiKey;
    }

    /**
     * 模型API Key
     */
    public void setModelApiKey(String modelApiKey) {
        this.modelApiKey = modelApiKey;
    }

    /**
     * 模型API密钥
     */
    public String getModelApiSecret() {
        return modelApiSecret;
    }

    /**
     * 模型API密钥
     */
    public void setModelApiSecret(String modelApiSecret) {
        this.modelApiSecret = modelApiSecret;
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
     * 模型状态，0未激活1已激活
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 模型状态，0未激活1已激活
     */
    public void setStatus(Integer status) {
        this.status = status;
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