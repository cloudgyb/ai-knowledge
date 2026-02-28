package com.github.cloudgyb.ai.knowledge.server.modules.ai.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 用户自定义的 AI 模型高级配置，包含模型温度、最大回复等
 *
 * @author cloudgyb
 * @since 2026-02-28 15:38:56
 */
@TableName(value = "ai_model_config")
public class AiModelConfig {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户创建的 AI 模型id
     */
    @TableField(value = "model_id")
    private Integer modelId;

    /**
     * 模型温度
     */
    @TableField(value = "temperature")
    private Double temperature;

    /**
     * 词汇属性
     */
    @TableField(value = "lexical")
    private Double lexical;

    /**
     * 话题属性
     */
    @TableField(value = "talk")
    private Double talk;

    /**
     * 重复属性
     */
    @TableField(value = "repeat")
    private Double repeat;

    /**
     * 最大回复的token数
     */
    @TableField(value = "tokens")
    private Integer tokens;

    /**
     * 等待AI响应的最长时间，单位为秒。
     */
    @TableField(value = "timeout")
    private Integer timeout;


    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "update_time")
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 用户创建的AI 模型id
     */
    public Integer getModelId() {
        return modelId;
    }

    /**
     * 用户创建的AI 模型id
     */
    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    /**
     * 模型温度
     */
    public Double getTemperature() {
        return temperature;
    }

    /**
     * 模型温度
     */
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    /**
     * 词汇属性
     */
    public Double getLexical() {
        return lexical;
    }

    /**
     * 词汇属性
     */
    public void setLexical(Double lexical) {
        this.lexical = lexical;
    }

    /**
     * 话题属性
     */
    public Double getTalk() {
        return talk;
    }

    /**
     * 话题属性
     */
    public void setTalk(Double talk) {
        this.talk = talk;
    }

    /**
     * 重复属性
     */
    public Double getRepeat() {
        return repeat;
    }

    /**
     * 重复属性
     */
    public void setRepeat(Double repeat) {
        this.repeat = repeat;
    }

    /**
     * 最大回复的token数
     */
    public Integer getTokens() {
        return tokens;
    }

    /**
     * 最大回复的token数
     */
    public void setTokens(Integer tokens) {
        this.tokens = tokens;
    }

    /**
     * 等待AI响应的最长时间，单位为秒。
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * 等待AI响应的最长时间，单位为秒。
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
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