package com.education.platform.dto.auth;

import com.education.platform.entities.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SignUpRequest {

    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 128)
    private String password;

    private LocalDate dateOfBirth;

    private Role role;

    private String firstName;
    private String lastName;
    private List<String> interests = new ArrayList<>();
    private String description;
    private String recuperationEmail;
}
