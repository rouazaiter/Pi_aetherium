package com.education.platform.services.interfaces;

import com.education.platform.entities.User;

public interface ProfileAccessService {

    void sendVerificationCode(User user);

    void verifyCode(User user, String code);

    void requireProfileAccess(User user);
}
