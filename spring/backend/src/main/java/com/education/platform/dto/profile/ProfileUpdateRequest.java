package com.education.platform.dto.profile;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProfileUpdateRequest {

    private String firstName;
    private String lastName;
    private List<String> interests = new ArrayList<>();
    private String description;
    private String profilePicture;

    @Email(message = "E-mail de récupération invalide")
    private String recuperationEmail;

    private Boolean twoFactorEnabled;
    private Boolean activeStatusVisible;
}
