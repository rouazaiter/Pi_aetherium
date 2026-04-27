package com.education.platform.dto.cv;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CVPreviewResponse {

    private CVPreviewProfileDto profile;
    private List<CVPreviewSkillGroupDto> skillsByCategory;
    private List<CVPreviewProjectDto> projects;
    private List<CVEducationDto> education;
    private List<CVExperienceDto> experience;
    private List<CVLanguageDto> languages;
    private CVPreviewMetaDto meta;
}
