package com.education.platform.services.interfaces;

import com.education.platform.dto.profile.ProfileResponse;
import com.education.platform.dto.profile.ProfileUpdateRequest;
import com.education.platform.dto.profile.LoginActivityResponse;
import com.education.platform.entities.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfileService {

    ProfileResponse getForUser(User user);

    ProfileResponse updateForUser(User user, ProfileUpdateRequest request);

    ProfileResponse updateProfilePhoto(User user, MultipartFile file);

    ProfileResponse updateTwoFactorForUser(User user, boolean enabled);

    ProfileResponse updateActiveStatusVisibilityForUser(User user, boolean enabled);

    List<LoginActivityResponse> getLoginActivityForUser(User user);
}
