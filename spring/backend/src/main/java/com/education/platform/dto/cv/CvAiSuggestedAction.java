package com.education.platform.dto.cv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CvAiSuggestedAction {

    private CvAiSuggestedActionType type;
    private String sectionType;
    private Long sectionId;
    private String field;
    private String newValue;
}
