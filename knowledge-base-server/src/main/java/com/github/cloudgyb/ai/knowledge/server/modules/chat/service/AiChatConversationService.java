package com.github.cloudgyb.ai.knowledge.server.modules.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.ConversationStatus;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatConversation;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * AI 聊天对话服务实现
 *
 * @author cloudgyb
 * @since 2026/03/24 16:09
 */
@Service
public class AiChatConversationService {
    private final ChatConversationService chatConversationService;

    public AiChatConversationService(ChatConversationService chatConversationService) {
        this.chatConversationService = chatConversationService;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long addConversation(String title) {
        title = StringUtils.isBlank(title) ? "新对话" : title;
        ChatConversation chatConversation = new ChatConversation();
        chatConversation.setUserId(1L);
        chatConversation.setTitle(title);
        chatConversation.setLastActiveTime(new Date());
        chatConversation.setCurrentStatus(ConversationStatus.NEW);
        if (!chatConversationService.save(chatConversation)) {
            throw new BusinessException("创建对话失败！");
        }
        return chatConversation.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long id) {
        if (!chatConversationService.removeById(id)) {
            throw new BusinessException("删除对话失败！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateConversation(Long id, String title) {
        ChatConversation chatConversation = new ChatConversation();
        chatConversation.setId(id);
        chatConversation.setTitle(title);
        if (!chatConversationService.updateById(chatConversation)) {
            throw new BusinessException("更新对话失败！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateConversation(Long id, Date lastActiveTime) {
        ChatConversation chatConversation = new ChatConversation();
        chatConversation.setId(id);
        chatConversation.setLastActiveTime(lastActiveTime);
        if (!chatConversationService.updateById(chatConversation)) {
            throw new BusinessException("更新对话失败！");
        }
    }

    public Page<ChatConversation> page(int pageNum, int pageSize) {
        LambdaQueryWrapper<ChatConversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatConversation::getUserId, 1L);
        queryWrapper.orderByDesc(ChatConversation::getLastActiveTime);
        queryWrapper.orderByDesc(ChatConversation::getUpdateTime);
        queryWrapper.orderByDesc(ChatConversation::getCreateTime);
        return chatConversationService.page(Page.of(pageNum, pageSize), queryWrapper);
    }

    public void getConversationMsgs(Long id) {
        ChatConversation chatConversation = chatConversationService.getById(id);
        if (chatConversation == null) {
            throw new BusinessException("对话不存在！");
        }
    }
}
