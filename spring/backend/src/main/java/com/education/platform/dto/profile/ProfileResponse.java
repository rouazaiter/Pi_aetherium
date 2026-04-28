package com.education.platform.dto.profile;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ProfileResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private List<String> interests;
    private String description;
    private String profilePicture;
    private Instant lastPasswordChanged;
    private String recuperationEmail;
    private boolean twoFactorEnabled;
    private boolean activeStatusVisible;
}
