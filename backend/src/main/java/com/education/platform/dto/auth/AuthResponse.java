package com.education.platform.dto.auth;

import com.education.platform.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private Long userId;
    private String username;
    private String email;
    private Role role;
}
