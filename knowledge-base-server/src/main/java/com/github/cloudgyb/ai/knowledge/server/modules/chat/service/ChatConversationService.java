package com.github.cloudgyb.ai.knowledge.server.modules.chat.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.ConversationStatus;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatConversation;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.mapper.ChatConversationMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 对话管理
 *
 * @author cloudgyb
 * @since 2026-03-24 15:42:15
 */
@Service
public class ChatConversationService extends ServiceImpl<ChatConversationMapper, ChatConversation> {

    public void updateConversationStatus(Long id, ConversationStatus status, String title, Date lastActiveTime) {
        ChatConversation chatConversation = new ChatConversation();
        chatConversation.setId(id);
        if (status == null && title == null && lastActiveTime == null) {
            throw new IllegalArgumentException("参数错误");
        }
        if (title != null) {
            chatConversation.setTitle(title);
        }
        if (lastActiveTime != null) {
            chatConversation.setLastActiveTime(lastActiveTime);
        }
        if (status != null) {
            chatConversation.setCurrentStatus(status);
        }
        this.updateById(chatConversation);
    }
}




