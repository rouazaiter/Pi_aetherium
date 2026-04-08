package com.education.platform.oauth;

public record GoogleUserClaims(
        String subject,
        String email,
        boolean emailVerified,
        String givenName,
        String familyName
) {
}
