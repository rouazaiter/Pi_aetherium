package com.education.platform.dto.cv;

import com.education.platform.entities.portfolio.Visibility;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CVProfileResponse {

    private Long id;
    private String headline;
    private String summary;
    private String professionalSummary;
    private String phone;
    private String location;
    private String preferredTemplate;
    private String language;
    private Visibility visibility;
    private List<Long> selectedProjectIds;
    private List<CVEducationDto> education;
    private List<CVExperienceDto> experience;
    private List<CVLanguageDto> languages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
