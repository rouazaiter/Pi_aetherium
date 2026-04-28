package com.education.platform.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "Identifiant requis")
    private String usernameOrEmail;

    @NotBlank(message = "Mot de passe requis")
    private String password;
}
