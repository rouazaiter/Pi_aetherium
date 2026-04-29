package com.education.platform.dto.admin.dashboard;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserStatusUpdateRequest {

    @NotNull
    private Boolean active;
}
