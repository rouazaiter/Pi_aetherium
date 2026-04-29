package com.education.platform.dto.cv;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CVPreviewMetaDto {

    private int estimatedPages;
    private boolean exceedsTwoPages;
    private String warning;
}
