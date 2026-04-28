package com.education.platform.dto.reclamation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReclamationRequest {

    @NotBlank(message = "Sujet requis")
    @Size(max = 255, message = "Sujet trop long")
    private String subject;

    @NotBlank(message = "Description requise")
    @Size(max = 16000, message = "Description trop longue")
    private String description;
}
