package com.education.platform.dto.portfolio.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorChatResponseDto {
    private String mainMessage;
    private List<String> notes;
    private List<String> rewrites;
    private String rawResponse;
}
