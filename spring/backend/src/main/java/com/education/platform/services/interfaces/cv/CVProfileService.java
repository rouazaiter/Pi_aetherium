package com.education.platform.services.interfaces.cv;

import com.education.platform.dto.cv.CVProfileResponse;
import com.education.platform.dto.cv.UpdateCVProfileRequest;
import com.education.platform.entities.User;
import com.education.platform.entities.cv.CVProfile;

import java.util.Optional;

public interface CVProfileService {

    CVProfileResponse getForUser(User user);

    CVProfileResponse updateForUser(User user, UpdateCVProfileRequest request);

    Optional<CVProfile> findEntityForUser(User user);
}
