package com.education.platform.dto.cv;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateCVDraftRequest {

    private String theme;

    private JsonNode settings;

    @Valid
    private List<UpdateCVSectionRequest> sections;
}
