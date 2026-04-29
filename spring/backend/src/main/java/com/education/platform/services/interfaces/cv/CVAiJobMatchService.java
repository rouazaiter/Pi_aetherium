package com.education.platform.services.interfaces.cv;

import com.education.platform.dto.cv.CvAiJobMatchRequest;
import com.education.platform.dto.cv.CvAiJobMatchResponse;
import com.education.platform.entities.User;

public interface CVAiJobMatchService {

    CvAiJobMatchResponse matchForUser(User user, CvAiJobMatchRequest request);
}
