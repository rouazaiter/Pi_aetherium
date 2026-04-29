package com.education.platform.services.interfaces.cv;

import com.education.platform.dto.cv.CvAiImproveRequest;
import com.education.platform.dto.cv.CvAiImproveResponse;
import com.education.platform.entities.User;

public interface CVAiImprovementService {

    CvAiImproveResponse improveForUser(User user, CvAiImproveRequest request);
}
