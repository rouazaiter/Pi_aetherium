package com.education.platform.services.interfaces.cv;

import com.education.platform.dto.cv.CVPreviewOptions;
import com.education.platform.dto.cv.CVPreviewResponse;
import com.education.platform.entities.User;

public interface CVBuilderService {

    CVPreviewResponse buildForUser(User user);

    CVPreviewResponse buildForUser(User user, CVPreviewOptions options);
}
