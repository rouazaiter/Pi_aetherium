package com.education.platform.dto.portfolio.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorChatRequestDto {
    @NotBlank
    @Size(max = 4000)
    private String message;
    @Size(max = 50)
    private String target;
    @Size(max = 50)
    private String replyMode;
}
