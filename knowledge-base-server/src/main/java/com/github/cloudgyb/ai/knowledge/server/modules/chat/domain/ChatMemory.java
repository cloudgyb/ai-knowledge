package com.github.cloudgyb.ai.knowledge.server.modules.chat.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * 对话记忆存储
 *
 * @author cloudgyb
 * @since 2026-04-26 18:19:05
 */
@TableName(value = "chat_memory")
@Getter
@Setter
public class ChatMemory {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 对话id
     */
    private Long cid;

    /**
     * chat message
     */
    private String message;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;
}