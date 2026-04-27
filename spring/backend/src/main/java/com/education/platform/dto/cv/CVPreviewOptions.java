package com.education.platform.dto.cv;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CVPreviewOptions {

    private String template;
    private String language;
    private Integer projectLimit;
}
