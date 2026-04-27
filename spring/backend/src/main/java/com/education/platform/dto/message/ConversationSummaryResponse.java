package com.education.platform.dto.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConversationSummaryResponse {
    private final Long conversationId;
    private final int analyzedTextMessages;
    private final String summary;
}

