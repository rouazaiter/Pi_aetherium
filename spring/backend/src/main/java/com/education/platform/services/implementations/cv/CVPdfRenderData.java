package com.education.platform.services.implementations.cv;

import com.education.platform.entities.cv.CVSectionType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CVPdfRenderData {

    private Long userId;
    private Long draftId;
    private String theme;
    private JsonNode settings;
    private List<Section> sections;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class Section {

        private CVSectionType type;
        private String title;
        private Integer orderIndex;
        private boolean visible;
        private JsonNode content;
    }
}
