package com.education.platform.services.interfaces;

import com.education.platform.dto.auth.AuthResponse;
import com.education.platform.dto.auth.LoginRequest;
import com.education.platform.entities.User;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse toAuthResponse(User user);
}
