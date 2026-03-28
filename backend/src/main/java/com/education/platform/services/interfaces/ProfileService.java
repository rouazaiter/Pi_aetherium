package com.education.platform.services.interfaces;

import com.education.platform.dto.profile.ProfileResponse;
import com.education.platform.dto.profile.ProfileUpdateRequest;
import com.education.platform.entities.User;

public interface ProfileService {

    ProfileResponse getForUser(User user);

    ProfileResponse updateForUser(User user, ProfileUpdateRequest request);
}
