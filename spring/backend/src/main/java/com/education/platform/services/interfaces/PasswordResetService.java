package com.education.platform.services.interfaces;

public interface PasswordResetService {

    void requestReset(String email);

    void resetPassword(String token, String newPassword);
}
