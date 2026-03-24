package com.github.cloudgyb.ai.knowledge.server.modules.chat.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 对话消息AI回复消息实体类
 *
 * @author cloudgyb
 * @since 2026/03/24 16:09
 */
@TableName(value = "chat_message_content")
@Data
public class ChatMessageAiContent {
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 对话id
     */
    private Long cid;

    /**
     * 用户单次对话id，关联 chat_message_user 表 id
     */
    private Long muId;

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
    private Object metadata;
}