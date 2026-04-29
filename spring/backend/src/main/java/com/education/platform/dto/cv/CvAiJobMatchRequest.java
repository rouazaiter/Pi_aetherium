package com.education.platform.dto.cv;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CvAiJobMatchRequest {

    @NotNull
    private Long draftId;

    @NotBlank
    @Size(max = 255)
    private String targetJobTitle;

    @NotBlank
    @Size(max = 20000)
    private String jobDescription;

    @Size(max = 100)
    private String tone;

    @Size(max = 32)
    private String language;
}
