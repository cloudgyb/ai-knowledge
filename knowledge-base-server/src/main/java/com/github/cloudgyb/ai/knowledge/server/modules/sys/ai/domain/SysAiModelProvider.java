package com.github.cloudgyb.ai.knowledge.server.modules.sys.ai.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 系统内置支持的 AI 模型提供商
 */
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
     * 模型提供商名称
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * 模型提供商名称
     */
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    /**
     * 模型提供商公司名称
     */
    public String getProviderCompany() {
        return providerCompany;
    }

    /**
     * 模型提供商公司名称
     */
    public void setProviderCompany(String providerCompany) {
        this.providerCompany = providerCompany;
    }

    /**
     * 模型url
     */
    public String getModelUrl() {
        return modelUrl;
    }

    /**
     * 模型url
     */
    public void setModelUrl(String modelUrl) {
        this.modelUrl = modelUrl;
    }

    /**
     * logo 图片，可以是url或base64编码
     */
    public String getLogoUrl() {
        return logoUrl;
    }

    /**
     * logo 图片，可以是url或base64编码
     */
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
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

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        SysAiModelProvider other = (SysAiModelProvider) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getProviderName() == null ? other.getProviderName() == null : this.getProviderName().equals(other.getProviderName()))
                && (this.getProviderCompany() == null ? other.getProviderCompany() == null : this.getProviderCompany().equals(other.getProviderCompany()))
                && (this.getModelUrl() == null ? other.getModelUrl() == null : this.getModelUrl().equals(other.getModelUrl()))
                && (this.getLogoUrl() == null ? other.getLogoUrl() == null : this.getLogoUrl().equals(other.getLogoUrl()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
                && (this.getDeleted() == null ? other.getDeleted() == null : this.getDeleted().equals(other.getDeleted()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getProviderName() == null) ? 0 : getProviderName().hashCode());
        result = prime * result + ((getProviderCompany() == null) ? 0 : getProviderCompany().hashCode());
        result = prime * result + ((getModelUrl() == null) ? 0 : getModelUrl().hashCode());
        result = prime * result + ((getLogoUrl() == null) ? 0 : getLogoUrl().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getDeleted() == null) ? 0 : getDeleted().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", providerName=").append(providerName);
        sb.append(", providerCompany=").append(providerCompany);
        sb.append(", modelUrl=").append(modelUrl);
        sb.append(", logoUrl=").append(logoUrl);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", deleted=").append(deleted);
        sb.append("]");
        return sb.toString();
    }
}