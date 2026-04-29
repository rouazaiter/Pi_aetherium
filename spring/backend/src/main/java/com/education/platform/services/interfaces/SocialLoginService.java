package com.education.platform.services.interfaces;

import com.education.platform.dto.auth.AuthResponse;
import com.education.platform.dto.auth.SocialLoginRequest;

public interface SocialLoginService {

    AuthResponse login(SocialLoginRequest request);
}
