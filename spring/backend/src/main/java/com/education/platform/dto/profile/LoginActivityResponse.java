package com.education.platform.dto.profile;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class LoginActivityResponse {

    private Instant loggedAt;
    private String ipAddress;
    private String userAgent;
}
