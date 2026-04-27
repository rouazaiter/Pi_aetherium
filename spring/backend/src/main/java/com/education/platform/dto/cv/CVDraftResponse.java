package com.education.platform.dto.cv;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CVDraftResponse {

    private Long id;
    private Long userId;
    private Long portfolioId;
    private String theme;
    private JsonNode settings;
    private List<CVSectionResponse> sections;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
