package com.education.platform.dto.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageStreamEvent {
    private String type;
    private Long conversationId;
    private Long messageId;
    private Long senderId;
    private Long recipientId;
}
