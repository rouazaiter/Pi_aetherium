package com.education.platform.dto.cv;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CVPreviewProfileDto {

    private String fullName;
    private String email;
    private String phone;
    private String location;
    private String profilePicture;
    private String headline;
    private String summary;
    private String githubUrl;
    private String linkedInUrl;
    private String preferredTemplate;
    private String language;
    private com.education.platform.entities.portfolio.Visibility visibility;
}
