package com.github.cloudgyb.ai.knowledge.server.modules.chat.mapper;

import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatConversation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【chat_conversation(AI 聊天对话信息存储表)】的数据库操作Mapper
 *
 * @author cloudgyb
 * @since 2026-03-24 15:42:15
 */
@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {

}




