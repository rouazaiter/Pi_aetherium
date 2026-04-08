package com.education.platform.services.interfaces;

import com.education.platform.dto.auth.SignUpRequest;
import com.education.platform.entities.User;

public interface UserService {

    User register(SignUpRequest request);

    User requireByUsername(String username);

    User provisionFromGoogle(String googleSub, String email, boolean emailVerified, String givenName, String familyName);

    User provisionFromFacebook(String facebookUserId, String email, String firstName, String lastName);
}
