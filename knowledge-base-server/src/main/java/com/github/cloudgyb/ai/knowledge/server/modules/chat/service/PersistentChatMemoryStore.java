package com.github.cloudgyb.ai.knowledge.server.modules.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.domain.ChatMemory;
import com.github.cloudgyb.ai.knowledge.server.modules.chat.mapper.ChatMemoryMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 持久化记忆存储
 *
 * @author cloudgyb
 * @since 2026/4/26 18:19
 */
@Service
public class PersistentChatMemoryStore implements ChatMemoryStore {
    private final Logger log = LoggerFactory.getLogger(getClass());
    public static final int maxMessages = 10;
    private final ChatMemoryMapper chatMemoryMapper;

    public PersistentChatMemoryStore(ChatMemoryMapper chatMemoryMapper) {
        this.chatMemoryMapper = chatMemoryMapper;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        if (!(memoryId instanceof Long mid)) {
            throw new IllegalArgumentException("memoryId must be Long");
        }
        if (log.isInfoEnabled()) {
            log.info("获取记忆 memoryId: {}", memoryId);
        }
        LambdaQueryWrapper<ChatMemory> queryWrapper = new LambdaQueryWrapper<ChatMemory>()
                .eq(ChatMemory::getCid, mid)
                .orderByDesc(ChatMemory::getCreateTime)
                .last("limit " + maxMessages);
        List<ChatMemory> chatMemories = chatMemoryMapper.selectList(queryWrapper);
        if (chatMemories != null && !chatMemories.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("获取了{}条记忆数据", chatMemories.size());
            }
            return chatMemories.stream().map(ChatMemory::getMessage)
                    .map(ChatMessageDeserializer::messageFromJson).toList();
        }
        return List.of();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (!(memoryId instanceof Long mid)) {
            throw new IllegalArgumentException("memoryId must be Long");
        }
        if (log.isInfoEnabled()) {
            log.info("更新记忆 memoryId: {}", memoryId);
            log.info("更新了{}条记忆数据", messages.size());
        }
        deleteMessages(memoryId);
        for (ChatMessage message : messages) {
            ChatMemory chatMemory = new ChatMemory();
            chatMemory.setCid(mid);
            chatMemory.setMessage(ChatMessageSerializer.messageToJson(message));
            chatMemoryMapper.insert(chatMemory);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteMessages(Object memoryId) {
        if (!(memoryId instanceof Long mid)) {
            throw new IllegalArgumentException("memoryId must be Long");
        }
        int delete = chatMemoryMapper.delete(new LambdaQueryWrapper<ChatMemory>().eq(ChatMemory::getCid, mid));

        if (log.isInfoEnabled()) {
            log.info("删除了{}条记忆数据", delete);
        }
    }
}
