package com.education.platform.dto.message;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ConversationResponse {
    private Long id;
    private Long otherUserId;
    private String otherUsername;
    private String otherFirstName;
    private String otherLastName;
    private Boolean otherActiveNow;
    private Instant otherLastActiveAt;
    private String lastMessage;
    private Instant lastMessageAt;
    private long unreadCount;
}
