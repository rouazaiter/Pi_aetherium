package com.education.platform.dto.cv;

import com.education.platform.entities.portfolio.Visibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UpdateCVProfileRequest {

    @Size(max = 255)
    private String headline;
    @Size(max = 3000)
    private String summary;
    @Size(max = 3000)
    private String professionalSummary;
    @Size(max = 32)
    private String phone;
    @Size(max = 255)
    private String location;
    @Size(max = 100)
    private String preferredTemplate;
    @Size(max = 16)
    private String language;
    private Visibility visibility;

    private List<Long> selectedProjectIds = new ArrayList<>();

    @Valid
    private List<CVEducationDto> education = new ArrayList<>();

    @Valid
    private List<CVExperienceDto> experience = new ArrayList<>();

    @Valid
    private List<CVLanguageDto> languages = new ArrayList<>();
}
