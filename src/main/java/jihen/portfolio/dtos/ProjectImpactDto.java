package jihen.portfolio.dtos;

import lombok.Builder;
import lombok.Data;

    @Data
    @Builder
    public class ProjectImpactDto {
        private Long projectId;
        private String title;
        private Integer impactScore;
        private String level;
        private Integer views;
        private Integer likes;
        private Boolean hasUrl;
        private Boolean isPinned;
    }

