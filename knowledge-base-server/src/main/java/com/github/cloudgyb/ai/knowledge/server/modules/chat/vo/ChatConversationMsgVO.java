package com.github.cloudgyb.ai.knowledge.server.modules.chat.vo;

import lombok.Data;

/**
 * 一个完整的对话消息 VO，包括用户的输入消息、AI的回复消息、AI的思考过程消息
 *
 * @author cloudgyb
 * @since 2026/4/14 20:23
 */
@Data
public class ChatConversationMsgVO {
    private String userInputMsg;
    private String aiReplyMsg;
    private String aiThinkingMsg;
}
