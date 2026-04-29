package com.education.platform.dto.admin.dashboard;

import com.education.platform.entities.AccountStatus;
import com.education.platform.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSummaryResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private AccountStatus accountStatus;
    private Instant createdAt;
    private Instant lastLogin;
}
