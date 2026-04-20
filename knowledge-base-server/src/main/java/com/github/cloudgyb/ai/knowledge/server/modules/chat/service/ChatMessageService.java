package com.github.cloudgyb.ai.knowledge.server.modules.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatMessageAiContent;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatMessageAiThinking;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatMessageMetadata;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatMessageUser;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.mapper.ChatMessageContentMapper;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.mapper.ChatMessageThinkingMapper;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.mapper.ChatMessageUserMapper;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.vo.ChatConversationMsgVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * 删除对话消息
     *
     * @param cid 对话 id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long cid) {
        this.remove(new LambdaQueryWrapper<ChatMessageUser>().eq(ChatMessageUser::getCid, cid));
        this.chatMessageThinkingMapper.delete(new LambdaQueryWrapper<ChatMessageAiThinking>()
                .eq(ChatMessageAiThinking::getCid, cid));
        this.chatMessageContentMapper.delete(new LambdaQueryWrapper<ChatMessageAiContent>()
                .eq(ChatMessageAiContent::getCid, cid));
    }


    /**
     * 获取对话消息(最近 10条)
     *
     * @param cid 对话 id
     */
    public List<ChatConversationMsgVO> getConversationMsgs(Long cid) {
        List<ChatConversationMsgVO> list = new ArrayList<>();
        // 获取用户消息最近10条
        LambdaQueryWrapper<ChatMessageUser> eq = new LambdaQueryWrapper<ChatMessageUser>()
                .eq(ChatMessageUser::getCid, cid).orderByDesc(ChatMessageUser::getSendAt)
                .last("LIMIT 10");
        List<ChatMessageUser> userMsgs = this.list(eq).reversed();
        for (ChatMessageUser userMsg : userMsgs) {
            ChatConversationMsgVO vo = new ChatConversationMsgVO();
            vo.setUserInputMsg(userMsg.getContent());
            ChatMessageAiContent chatMessageAiContent = this.chatMessageContentMapper.selectOne(
                    new LambdaQueryWrapper<ChatMessageAiContent>().eq(ChatMessageAiContent::getMuId, userMsg.getId()));
            if (chatMessageAiContent != null) {
                vo.setAiReplyMsg(chatMessageAiContent.getContent());
            }
            ChatMessageAiThinking chatMessageAiThinking = this.chatMessageThinkingMapper.selectOne(
                    new LambdaQueryWrapper<ChatMessageAiThinking>().eq(ChatMessageAiThinking::getMuId, userMsg.getId()));
            if (chatMessageAiThinking != null) {
                vo.setAiThinkingMsg(chatMessageAiThinking.getContent());
            }
            list.add(vo);
        }
        return list;
    }
}




