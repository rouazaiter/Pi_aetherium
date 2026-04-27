package com.education.platform.dto.message;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnsureConversationRequest {
    @NotNull
    private Long recipientId;
}
