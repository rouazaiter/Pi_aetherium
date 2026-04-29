package com.education.platform.dto.cv;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CVPreviewSkillGroupDto {

    private String category;
    private List<CVPreviewSkillDto> skills;
}
