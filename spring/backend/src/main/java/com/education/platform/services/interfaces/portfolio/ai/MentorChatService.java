package com.education.platform.services.interfaces.portfolio.ai;

import com.education.platform.dto.portfolio.ai.MentorChatRequestDto;
import com.education.platform.dto.portfolio.ai.MentorChatResponseDto;

public interface MentorChatService {
    MentorChatResponseDto chat(MentorChatRequestDto request);
}
