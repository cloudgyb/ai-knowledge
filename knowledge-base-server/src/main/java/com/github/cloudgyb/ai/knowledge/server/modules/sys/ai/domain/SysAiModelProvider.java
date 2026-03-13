package com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 系统内置支持的 AI 模型提供商
 */
@Getter
@Setter
@TableName(value = "sys_ai_model_provider")
public class SysAiModelProvider {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 模型提供商名称
     */
    @TableField(value = "provider_name")
    private String providerName;

    /**
     * 模型提供Code
     */
    @TableField(value = "provider_code")
    private String providerCode;

    /**
     * 模型提供商公司名称
     */
    @TableField(value = "provider_company")
    private String providerCompany;

    /**
     * 模型url
     */
    @TableField(value = "model_url")
    private String modelUrl;

    /**
     * logo 图片，可以是url或base64编码
     */
    @TableField(value = "logo_url")
    private String logoUrl;

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
}