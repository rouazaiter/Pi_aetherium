package com.education.platform.dto.cv;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CvAiChatRequest {

    @NotBlank
    @Size(max = 4000)
    private String message;

    private Long draftId;

    @NotNull
    private CvAiChatContextMode contextMode;
}
