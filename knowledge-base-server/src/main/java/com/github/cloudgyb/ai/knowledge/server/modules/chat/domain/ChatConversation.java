package com.github.cloudgyb.ai.knowledge.server.modules.chat.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import com.github.cloudgyb.ai.knowledge.server.modules.chat.ConversationStatus;
import lombok.Data;

/**
 * AI 聊天对话信息存储表
 *
 * @author cloudgyb
 * @since 2026/03/24 16:09
 */
@TableName(value = "chat_conversation")
@Data
public class ChatConversation {
    @TableId
    private Long id;

    private Long userId;

    /**
     * 对话标题
     */
    private String title;

    /**
     * 最后一次对话时间
     */
    private Date lastActiveTime;

    /**
     * 对话当前状态：NEW, ACTIVE，ENDED，FAILED
     */
    private ConversationStatus currentStatus;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;
}