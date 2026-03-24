package com.github.cloudgyb.ai.knowledge.server.modules.chat.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Date;

/**
 * 对话消息实体类
 *
 * @author cloudgyb
 * @since 2026/03/24 16:09
 */
@TableName(value = "chat_message_user")
@Data
public class ChatMessageUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 对话id
     */
    private Long cid;

    /**
     * 消息发送者：AI，USER
     */
    private String sender;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：TEXT
     */
    private String contentType;

    /**
     * 消息发送时间
     */
    private Date sendAt;

    /**
     * 元数据，token耗费等，业务决定具体细节
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ChatMessageMetadata metadata;
}