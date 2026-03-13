package com.github.cloudgyb.ai.knowledge.server.modules.ai.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * AI 模型配置存储表
 *
 * @author cloudgyb
 * @since 2026-02-28 15:01:50
 */
@Getter
@Setter
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
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "deleted")
    private Integer deleted;
}