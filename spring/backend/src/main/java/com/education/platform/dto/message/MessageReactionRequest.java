package com.education.platform.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageReactionRequest {

    @NotBlank
    private String emoji;
}
