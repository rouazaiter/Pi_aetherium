package com.education.platform.dto.social;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateStudyGroupRequest {

    @NotBlank(message = "Nom du groupe requis")
    private String name;

    private String description;

    private String topic;

    private String imageUrl;
}
