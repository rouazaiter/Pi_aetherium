package com.education.platform.dto.portfolio;

import com.education.platform.entities.portfolio.MediaType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectMediaRequest {

    private String mediaUrl;
    private MediaType mediaType;
    private Integer orderIndex;
}
