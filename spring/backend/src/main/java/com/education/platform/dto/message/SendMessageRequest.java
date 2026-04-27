package com.education.platform.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequest {

    @NotNull
    private Long recipientId;

    @NotBlank
    private String content;

    private Long replyToMessageId;
}
