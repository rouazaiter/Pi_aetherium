package com.education.platform.services.interfaces.cv;

import com.education.platform.dto.cv.CVDraftResponse;
import com.education.platform.dto.cv.UpdateCVDraftRequest;
import com.education.platform.entities.User;

public interface CVDraftService {

    CVDraftResponse generateForUser(User user);

    CVDraftResponse getLatestForUser(User user);

    CVDraftResponse updateLatestForUser(User user, UpdateCVDraftRequest request);

    CVDraftResponse updateForUser(User user, Long draftId, UpdateCVDraftRequest request);
}
