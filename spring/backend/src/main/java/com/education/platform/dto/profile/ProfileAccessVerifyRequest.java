package com.education.platform.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProfileAccessVerifyRequest {

    @NotBlank
    @Pattern(regexp = "\\d{6}", message = "Le code doit contenir 6 chiffres")
    private String code;
}
