package com.education.platform.dto.message;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class MessageResponse {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private String content;
    private String voiceUrl;
    private Instant sentAt;
    private String reactionEmoji;
    private Long replyToMessageId;
    private String replyToSnippet;
    private boolean deleted;
}
