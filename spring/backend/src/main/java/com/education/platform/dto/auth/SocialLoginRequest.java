package com.education.platform.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginRequest {

    @NotNull(message = "Fournisseur requis")
    private SocialProvider provider;

    @NotBlank(message = "Jeton requis")
    private String token;
}
