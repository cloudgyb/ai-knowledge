package com.github.cloudgyb.ai.knowledge.server.modules.chat.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatMessageAiContent;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatMessageAiThinking;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatMessageMetadata;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatMessageUser;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.mapper.ChatMessageContentMapper;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.mapper.ChatMessageThinkingMapper;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.mapper.ChatMessageUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 对话消息
 *
 * @author cloudgyb
 * @since 2026-03-24 15:46:07
 */
@Service
public class ChatMessageService extends ServiceImpl<ChatMessageUserMapper, ChatMessageUser> {
    private final ChatMessageThinkingMapper chatMessageThinkingMapper;
    private final ChatMessageContentMapper chatMessageContentMapper;

    public ChatMessageService(ChatMessageThinkingMapper chatMessageThinkingMapper,
                              ChatMessageContentMapper chatMessageContentMapper) {
        this.chatMessageThinkingMapper = chatMessageThinkingMapper;
        this.chatMessageContentMapper = chatMessageContentMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addUserMessage(ChatMessageUser message) {
        this.save(message);
    }

    @Transactional(rollbackFor = Exception.class)

    public void addThinkingMessage(ChatMessageAiThinking message) {
        this.chatMessageThinkingMapper.insert(message);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addContentMessage(ChatMessageAiContent message) {
        this.chatMessageContentMapper.insert(message);
    }
    @Transactional(rollbackFor = Exception.class)
    public void appendThinkingMessage(Long id, String message) {
        this.chatMessageThinkingMapper.append(id, message);
    }

    @Transactional(rollbackFor = Exception.class)
    public void appendContentMessage(Long id, String message) {
        this.chatMessageContentMapper.append(id, message);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMetadata(Long umId, ChatMessageMetadata chatMessageMetadata) {
        ChatMessageUser chatMessageUser = new ChatMessageUser();
        chatMessageUser.setId(umId);
        chatMessageUser.setMetadata(chatMessageMetadata);
        this.updateById(chatMessageUser);
    }
}




