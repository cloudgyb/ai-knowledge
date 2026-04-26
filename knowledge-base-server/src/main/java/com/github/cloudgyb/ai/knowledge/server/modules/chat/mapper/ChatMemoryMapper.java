package com.github.cloudgyb.ai.knowledge.server.modules.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatMemory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【chat_memory】的数据库操作Mapper
 *
 * @author cloudgyb
 * @since 2026-04-26 18:19:05
 */
@Mapper
public interface ChatMemoryMapper extends BaseMapper<ChatMemory> {

}
