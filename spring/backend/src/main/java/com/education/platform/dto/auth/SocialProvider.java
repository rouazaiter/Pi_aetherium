package com.education.platform.dto.auth;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SocialProvider {
    GOOGLE,
    FACEBOOK;

    @JsonCreator
    public static SocialProvider from(String value) {
        if (value == null) {
            return null;
        }
        return SocialProvider.valueOf(value.trim().toUpperCase());
    }
}
