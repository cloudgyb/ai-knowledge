export interface Conversation {
    id: string
    title: string
    lastActiveTime: string
}

export interface ChatConversationMsgVO {
    userInputMsg: string
    aiReplyMsg: string
    aiThinkingMsg: string
}
