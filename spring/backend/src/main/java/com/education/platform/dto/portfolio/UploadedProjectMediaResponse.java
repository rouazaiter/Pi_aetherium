package com.education.platform.dto.portfolio;

import com.education.platform.entities.portfolio.MediaType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadedProjectMediaResponse {

    private String mediaUrl;
    private MediaType mediaType;
    private String filename;
}
