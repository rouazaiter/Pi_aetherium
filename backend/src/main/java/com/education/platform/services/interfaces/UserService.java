package com.education.platform.services.interfaces;

import com.education.platform.dto.auth.SignUpRequest;
import com.education.platform.entities.User;

public interface UserService {

    User register(SignUpRequest request);

    User requireByUsername(String username);
}
