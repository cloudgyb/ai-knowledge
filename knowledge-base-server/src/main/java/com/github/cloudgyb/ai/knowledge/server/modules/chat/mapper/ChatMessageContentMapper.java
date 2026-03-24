package com.github.cloudgyb.ai.knowledge.server.modules.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatMessageAiContent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【chat_message_thinking】的数据库操作Mapper
 *
 * @author cloudgyb
 * @since 2026-03-24 15:46:07
 */
@Mapper
public interface ChatMessageContentMapper extends BaseMapper<ChatMessageAiContent> {

    boolean append(Long id, String message);
}




