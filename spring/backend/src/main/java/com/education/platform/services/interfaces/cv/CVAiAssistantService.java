package com.education.platform.services.interfaces.cv;

import com.education.platform.dto.cv.CvAiChatRequest;
import com.education.platform.dto.cv.CvAiChatResponse;
import com.education.platform.entities.User;

public interface CVAiAssistantService {

    CvAiChatResponse chatForUser(User user, CvAiChatRequest request);
}
